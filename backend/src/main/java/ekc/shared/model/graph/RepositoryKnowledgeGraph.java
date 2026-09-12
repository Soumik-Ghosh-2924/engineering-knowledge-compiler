package ekc.shared.model.graph;

import java.util.List;

public record RepositoryKnowledgeGraph(
        List<KnowledgeNode> nodes,
        List<KnowledgeEdge> edges,
        int totalTypeCount,
        boolean truncated) {
    public RepositoryKnowledgeGraph {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
    }

    public static RepositoryKnowledgeGraph empty() {
        return new RepositoryKnowledgeGraph(List.of(), List.of(), 0, false);
    }
}
