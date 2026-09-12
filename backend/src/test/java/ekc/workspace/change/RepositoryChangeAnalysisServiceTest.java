package ekc.workspace.change;

import ekc.compiler.repository.WorkspaceProvider;
import ekc.workspace.model.RepositoryRole;
import ekc.workspace.model.WorkspaceRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryChangeAnalysisServiceTest {
    @TempDir
    Path repositoryPath;

    @Test
    void comparesRefsAndReturnsEvidenceBasedRiskSignals() throws Exception {
        RevCommit base;
        RevCommit head;
        try (Git git = Git.init().setDirectory(repositoryPath.toFile()).call()) {
            Files.writeString(repositoryPath.resolve("pom.xml"), "<version>1</version>\n");
            git.add().addFilepattern(".").call();
            base = git.commit()
                    .setMessage("Initial build")
                    .setAuthor("EKC Test", "ekc@example.com")
                    .call();

            Files.writeString(repositoryPath.resolve("pom.xml"), "<version>2</version>\n");
            Path securityFile = repositoryPath.resolve("src/main/java/SecurityConfig.java");
            Files.createDirectories(securityFile.getParent());
            Files.writeString(securityFile, "class SecurityConfig {}\n");
            git.add().addFilepattern(".").call();
            head = git.commit()
                    .setMessage("Update dependency and security configuration")
                    .setAuthor("EKC Test", "ekc@example.com")
                    .call();
        }
        URI repositoryUri = URI.create("https://github.com/acme/app.git");
        WorkspaceProvider workspaceProvider = mock(WorkspaceProvider.class);
        when(workspaceProvider.resolveRepositoryLocation(repositoryUri)).thenReturn(repositoryPath);
        RepositoryChangeAnalysisService service = new RepositoryChangeAnalysisService(workspaceProvider);

        RepositoryChangeAnalysis result = service.analyze(new WorkspaceRepository(
                UUID.randomUUID(),
                repositoryUri,
                RepositoryRole.APPLICATION,
                true,
                base.name(),
                head.name(),
                null));

        assertThat(result.status()).isEqualTo(ChangeAnalysisStatus.ANALYZED);
        assertThat(result.commitCount()).isEqualTo(1);
        assertThat(result.changedFileCount()).isEqualTo(2);
        assertThat(result.additions()).isEqualTo(2);
        assertThat(result.deletions()).isEqualTo(1);
        assertThat(result.changedFiles()).extracting(ChangedFile::path)
                .containsExactlyInAnyOrder("pom.xml", "src/main/java/SecurityConfig.java");
        assertThat(result.riskSignals()).extracting(RiskSignal::category)
                .contains("DEPENDENCY_CHANGE", "SECURITY_REVIEW");
    }

    @Test
    void reportsAnUnresolvableRefWithoutDiscardingRepositoryAnalysis() throws Exception {
        try (Git ignored = Git.init().setDirectory(repositoryPath.toFile()).call()) {
        }
        URI repositoryUri = URI.create("https://github.com/acme/app.git");
        WorkspaceProvider workspaceProvider = mock(WorkspaceProvider.class);
        when(workspaceProvider.resolveRepositoryLocation(repositoryUri)).thenReturn(repositoryPath);
        RepositoryChangeAnalysisService service = new RepositoryChangeAnalysisService(workspaceProvider);

        RepositoryChangeAnalysis result = service.analyze(new WorkspaceRepository(
                UUID.randomUUID(),
                repositoryUri,
                RepositoryRole.APPLICATION,
                true,
                "main",
                "missing-branch",
                null));

        assertThat(result.status()).isEqualTo(ChangeAnalysisStatus.FAILED);
        assertThat(result.message()).contains("Ref could not be resolved");
    }
}
