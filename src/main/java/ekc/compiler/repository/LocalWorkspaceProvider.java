package ekc.compiler.repository;

import ekc.shared.config.CompilerWorkspaceProperties;
import ekc.shared.exception.WorkspaceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Local filesystem implementation of the compiler workspace.
 */
@Component
public class LocalWorkspaceProvider implements WorkspaceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalWorkspaceProvider.class);
    private final CompilerWorkspaceProperties properties;
    public LocalWorkspaceProvider(CompilerWorkspaceProperties properties) {
        this.properties = properties;
    }

    @Override
    public void initialize() {
        try {
            Files.createDirectories(getWorkspaceRoot());
            Files.createDirectories(getRepositoriesDirectory());
            Files.createDirectories(getTemporaryDirectory());
            LOGGER.info("Compiler workspace initialized.");
        } catch (IOException ex) {
            throw new WorkspaceInitializationException("Unable to initialize compiler workspace.", ex);
        }
    }

    @Override
    public Path resolveRepositoryLocation(URI repositoryUri) {

        String path = repositoryUri.getPath();
        String repositoryName = path.substring(path.lastIndexOf('/') + 1)
                .replace(".git", "");
        return getRepositoriesDirectory().resolve(repositoryName);
    }

    @Override
    public boolean repositoryExists(URI repositoryUri) {
        return Files.exists(resolveRepositoryLocation(repositoryUri));
    }

    private Path getWorkspaceRoot() {
        return Path.of(properties.getRoot());
    }

    private Path getRepositoriesDirectory() {
        return getWorkspaceRoot().resolve(properties.getRepositories());
    }

    private Path getTemporaryDirectory() {
        return getWorkspaceRoot().resolve(properties.getTemporary());
    }
}