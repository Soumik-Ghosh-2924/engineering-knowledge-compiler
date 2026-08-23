package ekc.compiler.repository;

import ekc.shared.exception.InvalidRepositoryException;
import ekc.shared.model.acquisition.CompileRepositoryRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

/**
 * Validates repository requests before they enter the compiler pipeline.
 * This component performs deterministic validation only. It never performs network or filesystem operations.
 */
@Component
public class RepositoryValidator {

    private static final Set<String> SUPPORTED_SCHEMES =
            Set.of("http", "https", "ssh");

    public CompileRepositoryRequest validate(CompileRepositoryRequest request) {
        if (request == null) {
            throw new InvalidRepositoryException(
                    "Repository request cannot be null.");
        }
        URI repositoryUri = request.getRepositoryUri();
        validateUri(repositoryUri);
        validateGitRepository(repositoryUri);
        return request;
    }

    private void validateUri(URI repositoryUri) {
        if (repositoryUri == null) {
            throw new InvalidRepositoryException(
                    "Repository URI cannot be null.");
        }
        String scheme = repositoryUri.getScheme();
        if (scheme == null || scheme.isBlank()) {
            throw new InvalidRepositoryException(
                    "Repository URI must specify a protocol.");
        }
        scheme = scheme.toLowerCase(Locale.ROOT);
        if (!SUPPORTED_SCHEMES.contains(scheme)) {
            throw new InvalidRepositoryException(
                    "Unsupported repository protocol: " + scheme);
        }
        if (repositoryUri.getHost() == null) {
            throw new InvalidRepositoryException("Repository URI must specify a host.");
        }
    }

    private void validateGitRepository(URI repositoryUri) {
        String host = repositoryUri.getHost().toLowerCase(Locale.ROOT);
        if (!Set.of("github.com", "gitlab.com").contains(host)) {
            throw new InvalidRepositoryException("Unsupported Git hosting provider: " + host);
        }
        String path = repositoryUri.getPath();
        if (path == null || path.isBlank()) {
            throw new InvalidRepositoryException("Repository path cannot be empty.");
        }
        if (!path.matches("^/.+/.+(\\.git)?$")) {
            throw new InvalidRepositoryException("Repository path is not valid.");
        }
    }
}