package ekc.compiler.repository;

import java.net.URI;
import java.nio.file.Path;

/**
 * Provides deterministic workspace locations for compiler resources.
 */
public interface WorkspaceProvider {

    /**
     * Creates the compiler workspace if it does not already exist.
     */
    void initialize();

    /**
     * Returns the location where the given repository should exist.
     * @param repositoryUri repository URI
     * @return local filesystem path
     */
    Path resolveRepositoryLocation(URI repositoryUri);

    /**
     * Returns true if the repository already exists locally.
     */
    boolean repositoryExists(URI repositoryUri);
}