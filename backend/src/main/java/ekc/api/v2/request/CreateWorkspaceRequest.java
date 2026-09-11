package ekc.api.v2.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateWorkspaceRequest(
        @NotBlank(message = "Workspace name must not be blank.") String name,
        @Valid @Size(min = 1, max = 5, message = "A workspace must contain between 1 and 5 repositories.")
        List<WorkspaceRepositoryRequest> repositories) {
}
