package ekc.workspace.change;

import ekc.compiler.repository.WorkspaceProvider;
import ekc.workspace.model.WorkspaceRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RepositoryChangeAnalysisService {
    private static final int MAX_COMMITS = 20;
    private static final int MAX_CHANGED_FILES = 100;
    private final WorkspaceProvider workspaceProvider;

    public RepositoryChangeAnalysisService(WorkspaceProvider workspaceProvider) {
        this.workspaceProvider = workspaceProvider;
    }

    public RepositoryChangeAnalysis analyze(WorkspaceRepository workspaceRepository) {
        if (workspaceRepository.baseRef() == null && workspaceRepository.headRef() == null) {
            return RepositoryChangeAnalysis.notRequested();
        }
        try {
            Path repositoryPath = workspaceProvider.resolveRepositoryLocation(workspaceRepository.repositoryUri());
            try (Git git = Git.open(repositoryPath.toFile())) {
                Repository repository = git.getRepository();
                fetchComparisonRefs(git, workspaceRepository);
                ObjectId base = resolve(repository, workspaceRepository.baseRef());
                ObjectId head = resolve(repository, workspaceRepository.headRef());
                List<ChangedFile> changedFiles = changedFiles(repository, base, head);
                CommitResult commits = commits(git, base, head);
                int additions = changedFiles.stream().mapToInt(ChangedFile::additions).sum();
                int deletions = changedFiles.stream().mapToInt(ChangedFile::deletions).sum();
                return new RepositoryChangeAnalysis(
                        ChangeAnalysisStatus.ANALYZED,
                        workspaceRepository.baseRef(),
                        workspaceRepository.headRef(),
                        base.name(),
                        head.name(),
                        commits.total(),
                        commits.truncated(),
                        changedFiles.size(),
                        additions,
                        deletions,
                        commits.items(),
                        changedFiles.stream().limit(MAX_CHANGED_FILES).toList(),
                        riskSignals(changedFiles),
                        "Repository changes compared without executing repository code.");
            }
        } catch (Exception exception) {
            return RepositoryChangeAnalysis.failed(
                    workspaceRepository.baseRef(),
                    workspaceRepository.headRef(),
                    safeMessage(exception));
        }
    }

    private void fetchComparisonRefs(
            Git git,
            WorkspaceRepository workspaceRepository) throws Exception {
        Repository repository = git.getRepository();
        if (repository.getConfig().getString("remote", "origin", "url") == null) return;
        List<RefSpec> refSpecs = new ArrayList<>();
        refSpecs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
        refSpecs.add(new RefSpec("+refs/tags/*:refs/tags/*"));
        addReviewRef(refSpecs, workspaceRepository.baseRef());
        addReviewRef(refSpecs, workspaceRepository.headRef());
        git.fetch().setRemote("origin").setRefSpecs(refSpecs).call();
    }

    private void addReviewRef(List<RefSpec> refSpecs, String ref) {
        if (ref.matches("^pull/\\d+(/head)?$")) {
            String source = ref.endsWith("/head") ? ref : ref + "/head";
            refSpecs.add(new RefSpec("+refs/" + source + ":refs/remotes/origin/" + ref));
        } else if (ref.matches("^merge-requests/\\d+/head$")) {
            refSpecs.add(new RefSpec("+refs/" + ref + ":refs/remotes/origin/" + ref));
        }
    }

    private ObjectId resolve(Repository repository, String ref) throws IOException {
        List<String> candidates = List.of(
                ref,
                "refs/remotes/origin/" + ref,
                "refs/heads/" + ref,
                "refs/tags/" + ref);
        for (String candidate : candidates) {
            ObjectId resolved = repository.resolve(candidate);
            if (resolved != null) return resolved;
        }
        throw new IllegalArgumentException("Ref could not be resolved: " + ref);
    }

    private List<ChangedFile> changedFiles(
            Repository repository,
            ObjectId base,
            ObjectId head) throws IOException {
        try (RevWalk walk = new RevWalk(repository);
             DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            RevCommit baseCommit = walk.parseCommit(base);
            RevCommit headCommit = walk.parseCommit(head);
            CanonicalTreeParser baseTree = new CanonicalTreeParser();
            CanonicalTreeParser headTree = new CanonicalTreeParser();
            try (var reader = repository.newObjectReader()) {
                baseTree.reset(reader, baseCommit.getTree().getId());
                headTree.reset(reader, headCommit.getTree().getId());
            }
            formatter.setRepository(repository);
            formatter.setDetectRenames(true);
            List<ChangedFile> files = new ArrayList<>();
            for (DiffEntry entry : formatter.scan(baseTree, headTree)) {
                int additions = 0;
                int deletions = 0;
                for (Edit edit : formatter.toFileHeader(entry).toEditList()) {
                    additions += edit.getEndB() - edit.getBeginB();
                    deletions += edit.getEndA() - edit.getBeginA();
                }
                String path = entry.getChangeType() == DiffEntry.ChangeType.DELETE
                        ? entry.getOldPath()
                        : entry.getNewPath();
                files.add(new ChangedFile(path, entry.getChangeType().name(), additions, deletions));
            }
            return List.copyOf(files);
        }
    }

    private CommitResult commits(Git git, ObjectId base, ObjectId head) throws Exception {
        List<CommitSummary> items = new ArrayList<>();
        int total = 0;
        for (RevCommit commit : git.log().addRange(base, head).setMaxCount(MAX_COMMITS + 1).call()) {
            total++;
            if (items.size() < MAX_COMMITS) {
                items.add(new CommitSummary(
                        commit.name(),
                        commit.abbreviate(8).name(),
                        commit.getShortMessage(),
                        commit.getAuthorIdent().getName(),
                        Instant.ofEpochSecond(commit.getCommitTime())));
            }
        }
        return new CommitResult(List.copyOf(items), total, total > MAX_COMMITS);
    }

    private List<RiskSignal> riskSignals(List<ChangedFile> files) {
        List<RiskSignal> signals = new ArrayList<>();
        addPathSignal(
                signals, files, "DEPENDENCY_CHANGE", "HIGH", "Dependency definition changed",
                "Review version changes, transitive dependencies, compatibility, licences, and known advisories.",
                this::isDependencyFile);
        addPathSignal(
                signals, files, "SECURITY_REVIEW", "HIGH", "Security-sensitive code changed",
                "This is a review signal, not a vulnerability finding. Validate authorization, secrets handling, and trust boundaries.",
                this::isSecuritySensitive);
        addPathSignal(
                signals, files, "DEPLOYMENT_CHANGE", "MEDIUM", "Deployment behaviour changed",
                "Review runtime configuration, rollout order, health checks, permissions, and rollback readiness.",
                this::isDeploymentFile);
        addPathSignal(
                signals, files, "DATA_CHANGE", "HIGH", "Database or migration asset changed",
                "Review backward compatibility, migration ordering, data safety, and rollback behaviour.",
                this::isDataFile);
        if (files.size() > 50) {
            signals.add(new RiskSignal(
                    "CHANGE_BREADTH",
                    "HIGH",
                    "Broad change surface",
                    "More than 50 files changed; split the review or increase regression coverage.",
                    List.of(files.size() + " changed files")));
        }
        return List.copyOf(signals);
    }

    private void addPathSignal(
            List<RiskSignal> signals,
            List<ChangedFile> files,
            String category,
            String severity,
            String title,
            String description,
            java.util.function.Predicate<String> predicate) {
        List<String> evidence = files.stream()
                .map(ChangedFile::path)
                .filter(predicate)
                .limit(8)
                .toList();
        if (!evidence.isEmpty()) {
            signals.add(new RiskSignal(category, severity, title, description, evidence));
        }
    }

    private boolean isDependencyFile(String path) {
        String name = fileName(path.toLowerCase(Locale.ROOT));
        return Set.of(
                "pom.xml", "build.gradle", "build.gradle.kts", "settings.gradle",
                "settings.gradle.kts", "package.json", "package-lock.json", "yarn.lock",
                "pnpm-lock.yaml", "requirements.txt", "pyproject.toml", "go.mod",
                "go.sum", "cargo.toml", "cargo.lock").contains(name);
    }

    private boolean isSecuritySensitive(String path) {
        String value = path.toLowerCase(Locale.ROOT);
        return List.of("security", "auth", "oauth", "jwt", "crypto", "secret", "credential")
                .stream().anyMatch(value::contains);
    }

    private boolean isDeploymentFile(String path) {
        String value = path.toLowerCase(Locale.ROOT);
        String name = fileName(value);
        return value.startsWith("k8s/")
                || value.startsWith("helm/")
                || value.startsWith(".github/workflows/")
                || name.equals("dockerfile")
                || name.startsWith("dockerfile.");
    }

    private boolean isDataFile(String path) {
        String value = path.toLowerCase(Locale.ROOT);
        return List.of("migration", "migrations", "liquibase", "flyway", "schema.sql")
                .stream().anyMatch(value::contains);
    }

    private String fileName(String path) {
        int separator = path.lastIndexOf('/');
        return separator < 0 ? path : path.substring(separator + 1);
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? "Repository change comparison failed."
                : message;
    }

    private record CommitResult(List<CommitSummary> items, int total, boolean truncated) {
    }
}
