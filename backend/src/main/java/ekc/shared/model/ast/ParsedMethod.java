package ekc.shared.model.ast;

import java.util.List;
import java.util.Objects;

public final class ParsedMethod {


    private final String name;
    private final String returnType;

    /**
     * Java modifiers.
     *
     * Example:
     * public
     * private
     * static
     * final
     */
    private final List<String> modifiers;

    /**
     * Fully qualified annotation names.
     */
    private final List<String> annotations;
    private final List<String> parameterTypes;

    public ParsedMethod(
            String name,
            String returnType,
            List<String> modifiers,
            List<String> annotations,
            List<String> parameterTypes) {

        this.name =
                Objects.requireNonNull(name);

        this.returnType =
                Objects.requireNonNull(returnType);

        this.modifiers =
                List.copyOf(
                        Objects.requireNonNull(modifiers));

        this.annotations =
                List.copyOf(
                        Objects.requireNonNull(annotations));

        this.parameterTypes =
                List.copyOf(
                        Objects.requireNonNull(parameterTypes));
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public String toString() {

        return "ParsedMethod{" +
                "name='" + name + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parameters=" + parameterTypes.size() +
                '}';
    }
}