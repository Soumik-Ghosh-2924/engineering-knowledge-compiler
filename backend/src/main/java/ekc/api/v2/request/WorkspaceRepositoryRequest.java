package ekc.api.v2.request;

import ekc.workspace.model.RepositoryRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkspaceRepositoryRequest(
        @NotBlank(message = "Repository URL must not be blank.") String repositoryUrl,
        @NotNull(message = "Repository role must be provided.") RepositoryRole role,
        boolean primary,
        String baseRef,
        String headRef,
        String declaredPurpose) {
}
