package ekc.api.v2.response;

import ekc.workspace.model.RepositoryRole;
import ekc.workspace.model.WorkspaceRepository;

import java.util.UUID;

public record WorkspaceRepositoryResponse(
        UUID id,
        String repositoryUrl,
        RepositoryRole role,
        boolean primary,
        String baseRef,
        String headRef) {
    static WorkspaceRepositoryResponse from(WorkspaceRepository repository) {
        return new WorkspaceRepositoryResponse(
                repository.id(),
                repository.repositoryUri().toString(),
                repository.role(),
                repository.primary(),
                repository.baseRef(),
                repository.headRef());
    }
}
