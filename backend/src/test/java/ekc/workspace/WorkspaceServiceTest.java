package ekc.workspace;

import ekc.api.v2.request.CreateWorkspaceRequest;
import ekc.api.v2.request.WorkspaceRepositoryRequest;
import ekc.compiler.repository.RepositoryValidator;
import ekc.workspace.model.RepositoryRole;
import ekc.workspace.model.Workspace;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceServiceTest {
    private final WorkspaceService service = new WorkspaceService(
            new RepositoryValidator(),
            Clock.fixed(Instant.parse("2026-09-11T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void createsAWorkspaceWithRelatedRepositories() {
        Workspace workspace = service.create(new CreateWorkspaceRequest(
                "EKC system",
                List.of(
                        repository("https://github.com/acme/app.git", RepositoryRole.APPLICATION, true),
                        repository("https://github.com/acme/deploy.git", RepositoryRole.DEPLOYMENT, false))));

        assertThat(workspace.name()).isEqualTo("EKC system");
        assertThat(workspace.repositories()).hasSize(2);
        assertThat(workspace.repositories()).filteredOn(repository -> repository.primary()).hasSize(1);
        assertThat(service.get(workspace.id())).isEqualTo(workspace);
    }

    @Test
    void requiresExactlyOnePrimaryRepository() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest(
                "Invalid",
                List.of(repository("https://github.com/acme/app.git", RepositoryRole.APPLICATION, false)));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly one repository must be marked as primary.");
    }

    private WorkspaceRepositoryRequest repository(String url, RepositoryRole role, boolean primary) {
        return new WorkspaceRepositoryRequest(url, role, primary, "main", null, null);
    }
}
