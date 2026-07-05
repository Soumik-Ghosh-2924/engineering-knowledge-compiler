package ekc.compiler.repository;

import java.net.URI;
import java.nio.file.Path;

/**
 * Acquires a Git repository into the compiler workspace.
 */
public interface RepositoryCloner {

    /**
     * Ensures that the repository exists locally.
     * @param repositoryUri remote repository URI
     * @param repositoryLocation local repository location
     */
    void load(
            URI repositoryUri,
            Path repositoryLocation
    );
}