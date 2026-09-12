package ekc.api.v2.response;

import ekc.shared.model.graph.KnowledgeVariable;

public record KnowledgeVariableResponse(String name, String type) {
    static KnowledgeVariableResponse from(KnowledgeVariable variable) {
        return new KnowledgeVariableResponse(variable.name(), variable.type());
    }
}
