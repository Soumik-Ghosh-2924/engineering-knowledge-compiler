package ekc.shared.model.graph;

import java.util.List;

public record KnowledgeMethod(
        String name,
        String returnType,
        List<String> annotations,
        List<KnowledgeVariable> parameters,
        List<KnowledgeVariable> localVariables) {
    public KnowledgeMethod {
        annotations = List.copyOf(annotations);
        parameters = List.copyOf(parameters);
        localVariables = List.copyOf(localVariables);
    }
}
