package ekc.api.v2.response;

import ekc.shared.model.graph.KnowledgeEdge;

public record KnowledgeEdgeResponse(
        String source,
        String target,
        String kind,
        String label,
        String confidence,
        String evidence) {
    static KnowledgeEdgeResponse from(KnowledgeEdge edge) {
        return new KnowledgeEdgeResponse(
                edge.source(), edge.target(), edge.kind(), edge.label(), edge.confidence(), edge.evidence());
    }
}
