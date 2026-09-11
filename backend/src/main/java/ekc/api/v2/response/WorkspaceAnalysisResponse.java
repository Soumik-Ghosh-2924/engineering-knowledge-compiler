package ekc.api.v2.response;

import ekc.workspace.analysis.WorkspaceAnalysis;
import ekc.workspace.analysis.WorkspaceAnalysisStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkspaceAnalysisResponse(
        UUID id,
        UUID workspaceId,
        WorkspaceAnalysisStatus status,
        Instant createdAt,
        Instant completedAt,
        List<RepositoryAnalysisStatusResponse> repositories) {
    public static WorkspaceAnalysisResponse from(WorkspaceAnalysis analysis) {
        return new WorkspaceAnalysisResponse(
                analysis.id(), analysis.workspaceId(), analysis.status(),
                analysis.createdAt(), analysis.completedAt(),
                analysis.repositories().stream().map(RepositoryAnalysisStatusResponse::from).toList());
    }
}
