package ekc.shared.model.ast;

import java.util.List;
import java.util.Objects;

/**
 * Compiler representation of a parsed Java compilation unit.
 *
 * This model is independent of JavaParser and contains only the
 * structural information required by subsequent compiler stages.
 */
public final class ParsedCompilationUnit {

    /**
     * Package declaration.
     * Null when the source file belongs to the default package.
     */
    private final String packageName;

    /**
     * Fully qualified import declarations.
     */
    private final List<String> imports;

    /**
     * Top-level types declared in this compilation unit.
     */
    private final List<ParsedType> types;

    public ParsedCompilationUnit(
            String packageName,
            List<String> imports,
            List<ParsedType> types) {

        this.packageName = packageName;
        this.imports = List.copyOf(
                Objects.requireNonNull(imports));

        this.types = List.copyOf(
                Objects.requireNonNull(types));
    }

    public String getPackageName() {
        return packageName;
    }

    public List<String> getImports() {
        return imports;
    }

    public List<ParsedType> getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return "ParsedCompilationUnit{" +
                "packageName='" + packageName + '\'' +
                ", imports=" + imports.size() +
                ", types=" + types.size() +
                '}';
    }
}