package ekc.api.v2.response;

import java.util.List;

public record PurposeStatementResponse(
        String value,
        String classification,
        String confidence,
        List<EvidenceCitationResponse> citations) {
    public PurposeStatementResponse {
        citations = List.copyOf(citations);
    }
}
