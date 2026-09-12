package ekc.shared.model.graph;

public record KnowledgeEdge(
        String source,
        String target,
        String kind,
        String label,
        String confidence,
        String evidence) {
}
