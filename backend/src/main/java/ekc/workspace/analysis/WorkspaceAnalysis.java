package ekc.workspace.analysis;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkspaceAnalysis(
        UUID id,
        UUID workspaceId,
        WorkspaceAnalysisStatus status,
        Instant createdAt,
        Instant completedAt,
        List<RepositoryAnalysisResult> repositories) {
    public WorkspaceAnalysis {
        repositories = List.copyOf(repositories);
    }
}
