package ekc.workspace.model;

import java.net.URI;
import java.util.UUID;

public record WorkspaceRepository(
        UUID id,
        URI repositoryUri,
        RepositoryRole role,
        boolean primary,
        String baseRef,
        String headRef,
        String declaredPurpose) {
}
