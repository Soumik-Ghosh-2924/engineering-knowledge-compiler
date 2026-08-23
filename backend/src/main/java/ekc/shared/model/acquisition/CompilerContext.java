package ekc.shared.model.acquisition;

import ekc.shared.model.ast.RepositoryAst;
import ekc.shared.model.analysis.CompilationDiagnostic;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.structure.RepositoryStructure;

import java.util.List;
import java.util.Objects;

/**
 * Shared immutable context flowing through every stage
 * of the compiler pipeline.
 */
public final class CompilerContext {

    private final RepositoryMetadata repositoryMetadata;

    private final RepositorySource repositorySource;

    private final RepositoryAst repositoryAst;

    private final RepositoryStructure repositoryStructure;

    private final List<CompilationDiagnostic> diagnostics;

    public CompilerContext(
            RepositoryMetadata repositoryMetadata,
            RepositorySource repositorySource,
            RepositoryAst repositoryAst,
            RepositoryStructure repositoryStructure) {

        this(repositoryMetadata, repositorySource, repositoryAst, repositoryStructure, List.of());
    }

    public CompilerContext(
            RepositoryMetadata repositoryMetadata,
            RepositorySource repositorySource,
            RepositoryAst repositoryAst,
            RepositoryStructure repositoryStructure,
            List<CompilationDiagnostic> diagnostics) {

        this.repositoryMetadata =
                Objects.requireNonNull(repositoryMetadata);

        this.repositorySource = repositorySource;

        this.repositoryAst = repositoryAst;

        this.repositoryStructure = repositoryStructure;
        this.diagnostics = List.copyOf(Objects.requireNonNull(diagnostics));
    }

    public RepositoryMetadata getRepositoryMetadata() {
        return repositoryMetadata;
    }

    public RepositorySource getRepositorySource() {
        return repositorySource;
    }

    public RepositoryAst getRepositoryAst() {
        return repositoryAst;
    }

    public RepositoryStructure getRepositoryStructure() {
        return repositoryStructure;
    }

    public List<CompilationDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public CompilerContext withRepositorySource(RepositorySource source) {
        return new CompilerContext(repositoryMetadata, source, repositoryAst, repositoryStructure, diagnostics);
    }

    public CompilerContext withRepositoryAst(
            RepositoryAst ast,
            List<CompilationDiagnostic> stageDiagnostics) {
        return new CompilerContext(
                repositoryMetadata,
                repositorySource,
                ast,
                repositoryStructure,
                appendDiagnostics(stageDiagnostics));
    }

    public CompilerContext withRepositoryStructure(
            RepositoryStructure structure,
            List<CompilationDiagnostic> stageDiagnostics) {
        return new CompilerContext(
                repositoryMetadata,
                repositorySource,
                repositoryAst,
                structure,
                appendDiagnostics(stageDiagnostics));
    }

    private List<CompilationDiagnostic> appendDiagnostics(
            List<CompilationDiagnostic> stageDiagnostics) {
        Objects.requireNonNull(stageDiagnostics);
        java.util.ArrayList<CompilationDiagnostic> combined = new java.util.ArrayList<>(diagnostics);
        combined.addAll(stageDiagnostics);
        return List.copyOf(combined);
    }

    @Override
    public String toString() {

        return "CompilerContext{" +
                "repositoryMetadata=" + repositoryMetadata +
                ", repositorySource=" + (repositorySource != null) +
                ", repositoryAst=" + (repositoryAst != null) +
                ", repositoryStructure=" + (repositoryStructure != null) +
                ", diagnostics=" + diagnostics.size() +
                '}';
    }
}
