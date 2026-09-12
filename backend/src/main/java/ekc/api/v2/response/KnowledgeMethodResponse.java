package ekc.api.v2.response;

import ekc.shared.model.graph.KnowledgeMethod;

import java.util.List;

public record KnowledgeMethodResponse(
        String name,
        String returnType,
        List<String> annotations,
        List<KnowledgeVariableResponse> parameters,
        List<KnowledgeVariableResponse> localVariables) {
    static KnowledgeMethodResponse from(KnowledgeMethod method) {
        return new KnowledgeMethodResponse(
                method.name(),
                method.returnType(),
                method.annotations(),
                method.parameters().stream().map(KnowledgeVariableResponse::from).toList(),
                method.localVariables().stream().map(KnowledgeVariableResponse::from).toList());
    }
}
