package ekc.shared.model.analysis;

import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.ast.ParsedCompilationUnit;
import ekc.shared.model.ast.ParsedType;
import ekc.shared.model.structure.ExtractedSourceFile;

import java.util.List;
import java.util.Objects;

/**
 * Structured, API-independent summary of a completed repository analysis.
 */
public final class CompilationSummary {

    private final String repositoryName;
    private final String defaultBranch;
    private final int sourceFiles;
    private final int parsedFiles;
    private final int extractedFiles;
    private final int packages;
    private final int imports;
    private final int types;
    private final int fields;
    private final int methods;
    private final int annotations;
    private final List<CompilationDiagnostic> diagnostics;

    private CompilationSummary(
            String repositoryName,
            String defaultBranch,
            int sourceFiles,
            int parsedFiles,
            int extractedFiles,
            int packages,
            int imports,
            int types,
            int fields,
            int methods,
            int annotations,
            List<CompilationDiagnostic> diagnostics) {

        this.repositoryName = Objects.requireNonNull(repositoryName);
        this.defaultBranch = Objects.requireNonNull(defaultBranch);
        this.sourceFiles = sourceFiles;
        this.parsedFiles = parsedFiles;
        this.extractedFiles = extractedFiles;
        this.packages = packages;
        this.imports = imports;
        this.types = types;
        this.fields = fields;
        this.methods = methods;
        this.annotations = annotations;
        this.diagnostics = List.copyOf(diagnostics);
    }

    public static CompilationSummary from(CompilerContext context) {
        List<ExtractedSourceFile> files = context.getRepositoryStructure().getSourceFiles();
        List<ParsedCompilationUnit> units = files.stream()
                .map(ExtractedSourceFile::getParsedCompilationUnit)
                .toList();
        List<ParsedType> parsedTypes = units.stream()
                .flatMap(unit -> unit.getTypes().stream())
                .toList();

        return new CompilationSummary(
                context.getRepositoryMetadata().getRepositoryName(),
                context.getRepositoryMetadata().getDefaultBranch(),
                context.getRepositorySource().getTotalSourceFiles(),
                context.getRepositoryAst().getTotalParsedFiles(),
                files.size(),
                (int) units.stream()
                        .map(ParsedCompilationUnit::getPackageName)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count(),
                units.stream().mapToInt(unit -> unit.getImports().size()).sum(),
                parsedTypes.size(),
                parsedTypes.stream().mapToInt(type -> type.getFields().size()).sum(),
                parsedTypes.stream().mapToInt(type -> type.getMethods().size()).sum(),
                parsedTypes.stream().mapToInt(type -> type.getAnnotations().size()).sum(),
                context.getDiagnostics()
        );
    }

    public String getRepositoryName() { return repositoryName; }
    public String getDefaultBranch() { return defaultBranch; }
    public int getSourceFiles() { return sourceFiles; }
    public int getParsedFiles() { return parsedFiles; }
    public int getExtractedFiles() { return extractedFiles; }
    public int getPackages() { return packages; }
    public int getImports() { return imports; }
    public int getTypes() { return types; }
    public int getFields() { return fields; }
    public int getMethods() { return methods; }
    public int getAnnotations() { return annotations; }
    public List<CompilationDiagnostic> getDiagnostics() { return diagnostics; }
}
