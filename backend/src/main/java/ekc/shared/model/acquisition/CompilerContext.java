package ekc.shared.model.acquisition;

import ekc.shared.model.ast.RepositoryAst;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.structure.RepositoryStructure;

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

    public CompilerContext(
            RepositoryMetadata repositoryMetadata,
            RepositorySource repositorySource,
            RepositoryAst repositoryAst,
            RepositoryStructure repositoryStructure) {

        this.repositoryMetadata =
                Objects.requireNonNull(repositoryMetadata);

        this.repositorySource = repositorySource;

        this.repositoryAst = repositoryAst;

        this.repositoryStructure = repositoryStructure;
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

    @Override
    public String toString() {

        return "CompilerContext{" +
                "repositoryMetadata=" + repositoryMetadata +
                ", repositorySource=" + (repositorySource != null) +
                ", repositoryAst=" + (repositoryAst != null) +
                ", repositoryStructure=" + (repositoryStructure != null) +
                '}';
    }
}