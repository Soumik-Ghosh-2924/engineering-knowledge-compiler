package ekc.api.v2.response;

import ekc.shared.model.graph.RepositoryKnowledgeGraph;

import java.util.List;

public record RepositoryKnowledgeGraphResponse(
        List<KnowledgeNodeResponse> nodes,
        List<KnowledgeEdgeResponse> edges,
        int totalTypeCount,
        boolean truncated) {
    public static RepositoryKnowledgeGraphResponse from(RepositoryKnowledgeGraph graph) {
        return new RepositoryKnowledgeGraphResponse(
                graph.nodes().stream().map(KnowledgeNodeResponse::from).toList(),
                graph.edges().stream().map(KnowledgeEdgeResponse::from).toList(),
                graph.totalTypeCount(),
                graph.truncated());
    }
}
