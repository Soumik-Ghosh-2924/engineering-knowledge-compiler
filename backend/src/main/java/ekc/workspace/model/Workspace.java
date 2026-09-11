package ekc.workspace.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Workspace(UUID id, String name, List<WorkspaceRepository> repositories, Instant createdAt) {
    public Workspace {
        repositories = List.copyOf(repositories);
    }
}
