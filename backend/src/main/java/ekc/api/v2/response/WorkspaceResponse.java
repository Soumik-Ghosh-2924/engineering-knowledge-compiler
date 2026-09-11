package ekc.api.v2.response;

import ekc.workspace.model.Workspace;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkspaceResponse(UUID id, String name, List<WorkspaceRepositoryResponse> repositories, Instant createdAt) {
    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.id(),
                workspace.name(),
                workspace.repositories().stream().map(WorkspaceRepositoryResponse::from).toList(),
                workspace.createdAt());
    }
}
