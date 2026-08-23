package ekc.shared.model.ast;

import java.util.List;
import java.util.Objects;


public final class ParsedField {
    private final String name;
    private final String type;

    /**
     * Java modifiers.
     *
     * Example:
     * private
     * final
     * static
     */
    private final List<String> modifiers;

    /**
     * Fully qualified annotation names.
     */
    private final List<String> annotations;

    public ParsedField(
            String name,
            String type,
            List<String> modifiers,
            List<String> annotations) {

        this.name =
                Objects.requireNonNull(name);

        this.type =
                Objects.requireNonNull(type);

        this.modifiers =
                List.copyOf(
                        Objects.requireNonNull(modifiers));

        this.annotations =
                List.copyOf(
                        Objects.requireNonNull(annotations));
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {

        return "ParsedField{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}