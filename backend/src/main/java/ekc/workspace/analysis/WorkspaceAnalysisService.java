package ekc.workspace.analysis;

import ekc.compiler.CompilerEngine;
import ekc.shared.model.acquisition.CompileRepositoryRequest;
import ekc.shared.model.analysis.CompilationResult;
import ekc.workspace.WorkspaceService;
import ekc.workspace.model.Workspace;
import ekc.workspace.model.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
public class WorkspaceAnalysisService {
    private final WorkspaceService workspaceService;
    private final CompilerEngine compilerEngine;
    private final Executor executor;
    private final Clock clock;
    private final ConcurrentHashMap<UUID, WorkspaceAnalysis> analyses = new ConcurrentHashMap<>();

    @Autowired
    public WorkspaceAnalysisService(
            WorkspaceService workspaceService,
            CompilerEngine compilerEngine,
            @Qualifier("phase2AnalysisExecutor") Executor executor) {
        this(workspaceService, compilerEngine, executor, Clock.systemUTC());
    }

    WorkspaceAnalysisService(
            WorkspaceService workspaceService,
            CompilerEngine compilerEngine,
            Executor executor,
            Clock clock) {
        this.workspaceService = workspaceService;
        this.compilerEngine = compilerEngine;
        this.executor = executor;
        this.clock = clock;
    }

    public WorkspaceAnalysis start(UUID workspaceId) {
        Workspace workspace = workspaceService.get(workspaceId);
        List<RepositoryAnalysisResult> queuedRepositories = workspace.repositories().stream()
                .map(repository -> new RepositoryAnalysisResult(
                        repository.id(), repository.repositoryUri().toString(),
                        "QUEUED", "Waiting for analysis.", null))
                .toList();
        WorkspaceAnalysis queued = new WorkspaceAnalysis(
                UUID.randomUUID(), workspaceId, WorkspaceAnalysisStatus.QUEUED,
                Instant.now(clock), null, queuedRepositories);
        analyses.put(queued.id(), queued);
        executor.execute(() -> analyze(queued.id(), workspace));
        return queued;
    }

    public WorkspaceAnalysis get(UUID analysisId) {
        WorkspaceAnalysis analysis = analyses.get(analysisId);
        if (analysis == null) {
            throw new NoSuchElementException("Analysis not found: " + analysisId);
        }
        return analysis;
    }

    private void analyze(UUID analysisId, Workspace workspace) {
        WorkspaceAnalysis queued = analyses.get(analysisId);
        analyses.put(analysisId, new WorkspaceAnalysis(
                queued.id(), queued.workspaceId(), WorkspaceAnalysisStatus.RUNNING,
                queued.createdAt(), null, queued.repositories()));

        List<RepositoryAnalysisResult> results = new ArrayList<>();
        for (WorkspaceRepository repository : workspace.repositories()) {
            try {
                CompilationResult result = compilerEngine.analyze(
                        new CompileRepositoryRequest(repository.repositoryUri()));
                results.add(new RepositoryAnalysisResult(
                        repository.id(), repository.repositoryUri().toString(),
                        result.getStatus(), result.getMessage(), result));
            } catch (RuntimeException exception) {
                results.add(new RepositoryAnalysisResult(
                        repository.id(), repository.repositoryUri().toString(),
                        "FAILED", safeMessage(exception), null));
            }
            List<RepositoryAnalysisResult> progress = new ArrayList<>(results);
            workspace.repositories().stream()
                    .skip(results.size())
                    .map(queuedRepository -> new RepositoryAnalysisResult(
                            queuedRepository.id(), queuedRepository.repositoryUri().toString(),
                            "QUEUED", "Waiting for analysis.", null))
                    .forEach(progress::add);
            analyses.put(analysisId, new WorkspaceAnalysis(
                    queued.id(), queued.workspaceId(), WorkspaceAnalysisStatus.RUNNING,
                    queued.createdAt(), null, List.copyOf(progress)));
        }

        long succeeded = results.stream().filter(result -> result.compilationResult() != null).count();
        WorkspaceAnalysisStatus status = succeeded == results.size()
                ? WorkspaceAnalysisStatus.COMPLETED
                : succeeded == 0 ? WorkspaceAnalysisStatus.FAILED : WorkspaceAnalysisStatus.PARTIALLY_COMPLETED;
        analyses.put(analysisId, new WorkspaceAnalysis(
                queued.id(), queued.workspaceId(), status, queued.createdAt(),
                Instant.now(clock), results));
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null ? "Repository analysis failed." : exception.getMessage();
    }
}
