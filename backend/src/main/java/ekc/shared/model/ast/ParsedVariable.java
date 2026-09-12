package ekc.shared.model.ast;

import java.util.Objects;

public record ParsedVariable(String name, String type) {
    public ParsedVariable {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
    }
}
