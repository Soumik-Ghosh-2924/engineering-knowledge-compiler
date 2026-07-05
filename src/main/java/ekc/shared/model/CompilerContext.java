package ekc.shared.model;

import java.util.Objects;

/**
 * Shared context flowing through every stage of the compiler pipeline.
 */
public final class CompilerContext {

    private final RepositoryMetadata repositoryMetadata;

    public CompilerContext(RepositoryMetadata repositoryMetadata) {
        this.repositoryMetadata =
                Objects.requireNonNull(repositoryMetadata);
    }

    public RepositoryMetadata getRepositoryMetadata() {
        return repositoryMetadata;
    }

    @Override
    public String toString() {
        return "CompilerContext{" +
                "repositoryMetadata=" + repositoryMetadata +
                '}';
    }
}