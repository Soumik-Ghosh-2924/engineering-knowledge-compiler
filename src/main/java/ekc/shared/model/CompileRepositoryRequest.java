package ekc.shared.model;

import java.net.URI;
import java.util.Objects;

/**
 * Represents a request to compile a Git repository.
 */
public final class CompileRepositoryRequest {

    private final URI repositoryUri;

    public CompileRepositoryRequest(URI repositoryUri) {
        this.repositoryUri = Objects.requireNonNull(
                repositoryUri,
                "Repository URI cannot be null."
        );
    }

    public URI getRepositoryUri() {
        return repositoryUri;
    }

    @Override
    public String toString() {
        return "CompileRepositoryRequest{" +
                "repositoryUri=" + repositoryUri +
                '}';
    }
}