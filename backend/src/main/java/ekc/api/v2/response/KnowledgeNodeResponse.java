package ekc.api.v2.response;

import ekc.shared.model.graph.KnowledgeNode;

import java.util.List;

public record KnowledgeNodeResponse(
        String id,
        String label,
        String kind,
        String qualifiedName,
        String packageName,
        String sourcePath,
        List<String> annotations,
        List<String> imports,
        List<KnowledgeFieldResponse> fields,
        List<KnowledgeMethodResponse> methods) {
    static KnowledgeNodeResponse from(KnowledgeNode node) {
        return new KnowledgeNodeResponse(
                node.id(), node.label(), node.kind(), node.qualifiedName(), node.packageName(), node.sourcePath(),
                node.annotations(), node.imports(),
                node.fields().stream().map(KnowledgeFieldResponse::from).toList(),
                node.methods().stream().map(KnowledgeMethodResponse::from).toList());
    }
}
