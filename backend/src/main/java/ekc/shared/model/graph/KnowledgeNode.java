package ekc.shared.model.graph;

import java.util.List;

public record KnowledgeNode(
        String id,
        String label,
        String kind,
        String qualifiedName,
        String packageName,
        String sourcePath,
        List<String> annotations,
        List<String> imports,
        List<KnowledgeField> fields,
        List<KnowledgeMethod> methods) {
    public KnowledgeNode {
        annotations = List.copyOf(annotations);
        imports = List.copyOf(imports);
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
    }
}
