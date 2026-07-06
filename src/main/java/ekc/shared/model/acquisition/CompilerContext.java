package ekc.shared.model.acquisition;

import ekc.shared.model.source.RepositorySource;

import java.util.Objects;

/**
 * Shared context flowing through every stage of the compiler pipeline.
 */
public final class CompilerContext {

    private final RepositoryMetadata repositoryMetadata;
    private final RepositorySource repositorySource;


    public CompilerContext(RepositoryMetadata repositoryMetadata) {
        this(repositoryMetadata, null);
    }

    /**
     * Constructor used after Source Discovery (Day-2).
     */
    public CompilerContext(RepositoryMetadata repositoryMetadata, RepositorySource repositorySource) {
        this.repositoryMetadata = Objects.requireNonNull(repositoryMetadata);
        this.repositorySource = repositorySource;
    }

    public RepositoryMetadata getRepositoryMetadata() {
        return repositoryMetadata;
    }

    public RepositorySource getRepositorySource() {
        return repositorySource;
    }

    @Override
    public String toString() {
        return "CompilerContext{" +
                "repositoryMetadata=" + repositoryMetadata +
                ", repositorySource=" + repositorySource +
                '}';
    }
}