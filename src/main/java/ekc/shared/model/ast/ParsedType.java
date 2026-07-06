package ekc.shared.model.ast;

import java.util.List;
import java.util.Objects;

/**
 * Compiler representation of a Java type.
 *
 * A type may represent a class, interface, enum,
 * record or annotation declaration.
 */
public final class ParsedType {

    /**
     * Simple type name.
     */
    private final String name;

    /**
     * Kind of Java type.
     */
    private final TypeKind kind;

    /**
     * Java modifiers.
     *
     * Example:
     * public
     * abstract
     * final
     */
    private final List<String> modifiers;

    /**
     * Fully qualified annotation names.
     */
    private final List<String> annotations;

    /**
     * Methods declared within this type.
     */
    private final List<ParsedMethod> methods;

    /**
     * Fields declared within this type.
     */
    private final List<ParsedField> fields;

    public ParsedType(
            String name,
            TypeKind kind,
            List<String> modifiers,
            List<String> annotations,
            List<ParsedMethod> methods,
            List<ParsedField> fields) {

        this.name =
                Objects.requireNonNull(name);

        this.kind =
                Objects.requireNonNull(kind);

        this.modifiers =
                List.copyOf(Objects.requireNonNull(modifiers));

        this.annotations =
                List.copyOf(Objects.requireNonNull(annotations));

        this.methods =
                List.copyOf(Objects.requireNonNull(methods));

        this.fields =
                List.copyOf(Objects.requireNonNull(fields));
    }

    public String getName() {
        return name;
    }

    public TypeKind getKind() {
        return kind;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public List<ParsedMethod> getMethods() {
        return methods;
    }

    public List<ParsedField> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "ParsedType{" +
                "name='" + name + '\'' +
                ", kind=" + kind +
                ", methods=" + methods.size() +
                ", fields=" + fields.size() +
                '}';
    }
}