package ekc.api.v2.response;

import java.util.List;

public record SystemOverviewResponse(
        int repositories,
        int analyzedRepositories,
        int failedRepositories,
        List<RepositoryRelationshipResponse> suggestedRelationships) {
    public SystemOverviewResponse {
        suggestedRelationships = List.copyOf(suggestedRelationships);
    }
}
