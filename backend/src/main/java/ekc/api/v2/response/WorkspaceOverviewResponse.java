package ekc.api.v2.response;

import java.util.List;
import java.util.UUID;

public record WorkspaceOverviewResponse(
        UUID analysisId,
        UUID workspaceId,
        String workspaceName,
        String status,
        List<RepositoryBriefResponse> repositoryBriefs,
        SystemOverviewResponse systemOverview) {
    public WorkspaceOverviewResponse {
        repositoryBriefs = List.copyOf(repositoryBriefs);
    }
}
