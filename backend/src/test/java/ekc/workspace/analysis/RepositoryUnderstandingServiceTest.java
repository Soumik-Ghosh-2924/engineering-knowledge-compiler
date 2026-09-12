package ekc.workspace.analysis;

import ekc.api.v2.response.PurposeStatementResponse;
import ekc.compiler.repository.WorkspaceProvider;
import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.acquisition.RepositoryMetadata;
import ekc.shared.model.analysis.CompilationSummary;
import ekc.shared.model.ast.RepositoryAst;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.structure.RepositoryStructure;
import ekc.workspace.model.RepositoryRole;
import ekc.workspace.model.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryUnderstandingServiceTest {
    private static final URI REPOSITORY_URI = URI.create("https://github.com/acme/app.git");

    @TempDir
    Path repositoryPath;

    @Test
    void usesTheFirstDescriptiveReadmeParagraphAsRepositoryUnderstanding() throws Exception {
        Files.writeString(repositoryPath.resolve("README.md"), """
                # Acme Checkout

                [![Build](https://img.shields.io/badge/build-passing-green)](https://example.com)

                Acme Checkout coordinates customer payments and gives support teams a reliable transaction view.

                ## Installation
                Run the application locally.
                """);

        PurposeStatementResponse result = service().understand(repository(), summary());

        assertThat(result.value()).isEqualTo(
                "Acme Checkout coordinates customer payments and gives support teams a reliable transaction view.");
        assertThat(result.classification()).isEqualTo("INFERRED");
        assertThat(result.confidence()).isEqualTo("HIGH");
        assertThat(result.citations()).singleElement().satisfies(citation -> {
            assertThat(citation.sourceType()).isEqualTo("README");
            assertThat(citation.source()).isEqualTo("README.md");
        });
    }

    @Test
    void fallsBackToCodeAnalyticsWhenNoReadmeExists() {
        PurposeStatementResponse result = service().understand(repository(), summary());

        assertThat(result.value()).contains(
                "application repository", "0 Java source files", "business consumers are not stated");
        assertThat(result.classification()).isEqualTo("INFERRED");
        assertThat(result.confidence()).isEqualTo("LOW");
        assertThat(result.citations()).singleElement()
                .extracting(citation -> citation.sourceType())
                .isEqualTo("STATIC_ANALYSIS");
    }

    private RepositoryUnderstandingService service() {
        WorkspaceProvider workspaceProvider = mock(WorkspaceProvider.class);
        when(workspaceProvider.resolveRepositoryLocation(REPOSITORY_URI)).thenReturn(repositoryPath);
        return new RepositoryUnderstandingService(workspaceProvider);
    }

    private WorkspaceRepository repository() {
        return new WorkspaceRepository(
                UUID.randomUUID(), REPOSITORY_URI, RepositoryRole.APPLICATION,
                true, null, null, "Legacy declared purpose must not replace evidence.");
    }

    private CompilationSummary summary() {
        CompilerContext context = new CompilerContext(
                new RepositoryMetadata("app", "main", repositoryPath),
                new RepositorySource(List.of(), Map.of()),
                new RepositoryAst(List.of()),
                new RepositoryStructure(List.of()));
        return CompilationSummary.from(context);
    }
}
