package ekc.compiler.repository;

import ekc.shared.exception.RepositoryAcquisitionException;
import ekc.shared.model.RepositoryMetadata;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

/**
 * JGit implementation of the RepositoryMetadataExtractor.
 * Responsible for extracting metadata from a locally available
 * Git repository.
 */
@Component
public class GitRepositoryMetadataExtractor
        implements RepositoryMetadataExtractor {

    @Override
    public RepositoryMetadata extract(Path repositoryLocation) {

        try (Git git = Git.open(repositoryLocation.toFile())) {
            Repository repository = git.getRepository();
            String repositoryName = repositoryLocation.getFileName().toString();
            String defaultBranch = repository.getBranch();

            return new RepositoryMetadata(
                    repositoryName,
                    defaultBranch,
                    repositoryLocation
            );

        } catch (IOException exception) {

            throw new RepositoryAcquisitionException(
                    "Failed to extract repository metadata from: "
                            + repositoryLocation,
                    exception
            );
        }
    }
}