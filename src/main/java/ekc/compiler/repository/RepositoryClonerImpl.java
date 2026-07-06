package ekc.compiler.repository;

import ekc.shared.exception.RepositoryAcquisitionException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JGit-based implementation of the RepositoryCloner.
 * This class encapsulates all interactions with JGit.
 */
@Component
public class RepositoryClonerImpl implements RepositoryCloner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RepositoryClonerImpl.class);

    @Override
    public void load(URI repositoryUri, Path repositoryLocation) {
        try {
            Path gitDirectory = repositoryLocation.resolve(".git");
            if (Files.notExists(gitDirectory)) {
                cloneRepository(repositoryUri, repositoryLocation);
                LOGGER.info("Repository cloned successfully: {}", repositoryUri);
                return;
            }
            updateRepository(repositoryLocation);
            LOGGER.info("Repository updated successfully: {}", repositoryUri);
        } catch (IOException | GitAPIException exception) {
            throw new RepositoryAcquisitionException("Unable to acquire repository: " + repositoryUri, exception);
        }
    }

    /**
     * Clones a repository into the local workspace.
     */
    private void cloneRepository(URI repositoryUri, Path repositoryLocation) throws GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository();

        cloneCommand.setURI(repositoryUri.toString())
                .setDirectory(repositoryLocation.toFile())
                .setCloneAllBranches(false);

        try (Git ignored = cloneCommand.call()) {
        }
    }

    /**
     * Fetches the latest objects from the remote.
     */
    private void updateRepository(Path repositoryLocation) throws IOException, GitAPIException {
        try (Git git =Git.open(repositoryLocation.toFile())) {
            git.fetch()
                    .setRemote("origin")
                    .call();
        }
    }
}