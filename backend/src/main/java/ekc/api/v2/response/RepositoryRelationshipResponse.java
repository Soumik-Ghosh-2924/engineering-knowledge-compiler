package ekc.api.v2.response;

import java.util.List;
import java.util.UUID;

public record RepositoryRelationshipResponse(
        UUID sourceRepositoryId,
        UUID targetRepositoryId,
        String relationship,
        String confidence,
        boolean confirmed,
        List<EvidenceCitationResponse> citations) {
    public RepositoryRelationshipResponse {
        citations = List.copyOf(citations);
    }
}
