package ekc.compiler.repository;

import ekc.shared.model.acquisition.RepositoryMetadata;

import java.nio.file.Path;

/**
 * Extracts metadata from a local Git repository.
 */
public interface RepositoryMetadataExtractor {

    RepositoryMetadata extract(
            Path repositoryLocation
    );
}