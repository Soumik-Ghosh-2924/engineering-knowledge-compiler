package ekc.shared.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Metadata describing a repository after it has been cloned or updated.
 */
public final class RepositoryMetadata {

    private final String repositoryName;
    private final String defaultBranch;
    private final Path localPath;

    public RepositoryMetadata(
            String repositoryName,
            String defaultBranch,
            Path localPath) {

        this.repositoryName = Objects.requireNonNull(repositoryName);
        this.defaultBranch = Objects.requireNonNull(defaultBranch);
        this.localPath = Objects.requireNonNull(localPath);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public Path getLocalPath() {
        return localPath;
    }

    @Override
    public String toString() {
        return "RepositoryMetadata{" +
                "repositoryName='" + repositoryName + '\'' +
                ", defaultBranch='" + defaultBranch + '\'' +
                ", localPath=" + localPath +
                '}';
    }
}