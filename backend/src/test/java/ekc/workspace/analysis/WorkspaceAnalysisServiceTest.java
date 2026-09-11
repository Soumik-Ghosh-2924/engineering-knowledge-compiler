package ekc.workspace.analysis;

import ekc.compiler.CompilerEngine;
import ekc.shared.model.analysis.CompilationResult;
import ekc.shared.model.analysis.CompilationSummary;
import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.acquisition.RepositoryMetadata;
import ekc.shared.model.ast.RepositoryAst;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.structure.RepositoryStructure;
import ekc.workspace.WorkspaceService;
import ekc.workspace.model.RepositoryRole;
import ekc.workspace.model.Workspace;
import ekc.workspace.model.WorkspaceRepository;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkspaceAnalysisServiceTest {

    @Test
    void preservesSuccessfulResultsWhenAnotherRepositoryFails() {
        WorkspaceService workspaceService = mock(WorkspaceService.class);
        CompilerEngine compilerEngine = mock(CompilerEngine.class);
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace(
                workspaceId,
                "Checkout system",
                List.of(
                        repository("https://github.com/acme/app.git", RepositoryRole.APPLICATION, true),
                        repository("https://github.com/acme/deployment.git", RepositoryRole.DEPLOYMENT, false)),
                Instant.parse("2026-09-11T00:00:00Z"));
        CompilationResult success = successfulResult();
        when(workspaceService.get(workspaceId)).thenReturn(workspace);
        when(compilerEngine.analyze(any()))
                .thenReturn(success)
                .thenThrow(new IllegalStateException("Repository unavailable."));

        WorkspaceAnalysisService service = new WorkspaceAnalysisService(
                workspaceService,
                compilerEngine,
                Runnable::run,
                Clock.fixed(Instant.parse("2026-09-11T00:01:00Z"), ZoneOffset.UTC));

        WorkspaceAnalysis analysis = service.get(service.start(workspaceId).id());

        assertThat(analysis.status()).isEqualTo(WorkspaceAnalysisStatus.PARTIALLY_COMPLETED);
        assertThat(analysis.repositories()).extracting(RepositoryAnalysisResult::status)
                .containsExactly("ANALYZED", "FAILED");
        assertThat(analysis.repositories().getFirst().compilationResult()).isSameAs(success);
    }

    private WorkspaceRepository repository(String url, RepositoryRole role, boolean primary) {
        return new WorkspaceRepository(
                UUID.randomUUID(), URI.create(url), role, primary, null, null, null);
    }

    private CompilationResult successfulResult() {
        CompilerContext context = new CompilerContext(
                new RepositoryMetadata("app", "main", Path.of("app")),
                new RepositorySource(List.of(), Map.of()),
                new RepositoryAst(List.of()),
                new RepositoryStructure(List.of()));
        return new CompilationResult(
                "ANALYZED", "Analysis complete.", 10, CompilationSummary.from(context));
    }
}
