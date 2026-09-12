package ekc.api.v2.response;

import ekc.shared.model.graph.KnowledgeField;

public record KnowledgeFieldResponse(String name, String type) {
    static KnowledgeFieldResponse from(KnowledgeField field) {
        return new KnowledgeFieldResponse(field.name(), field.type());
    }
}
