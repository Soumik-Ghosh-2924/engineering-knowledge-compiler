package ekc.compiler.repository;

import ekc.shared.model.CompileRepositoryRequest;
import ekc.shared.model.CompilerContext;
import ekc.shared.model.RepositoryMetadata;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Default implementation of the RepositoryLoader.
 * Orchestrates the complete Repository Acquisition stage
 * of the Engineering Knowledge Compiler.
 */
@Component
public class RepositoryLoaderImpl implements RepositoryLoader {

    private final RepositoryValidator repositoryValidator;
    private final WorkspaceProvider workspaceProvider;
    private final RepositoryCloner repositoryCloner;
    private final RepositoryMetadataExtractor metadataExtractor;

    public RepositoryLoaderImpl(
            RepositoryValidator repositoryValidator,
            WorkspaceProvider workspaceProvider,
            RepositoryCloner repositoryCloner,
            RepositoryMetadataExtractor metadataExtractor) {

        this.repositoryValidator = repositoryValidator;
        this.workspaceProvider = workspaceProvider;
        this.repositoryCloner = repositoryCloner;
        this.metadataExtractor = metadataExtractor;
    }

    @Override
    public CompilerContext load(CompileRepositoryRequest request) {

        /*
         * Stage 1
         * Validate incoming request.
         */
        CompileRepositoryRequest validatedRequest = repositoryValidator.validate(request);

        /*
         * Stage 2
         * Ensure compiler workspace exists.
         */
        workspaceProvider.initialize();

        /*
         * Stage 3
         * Resolve repository location inside the workspace.
         */
        Path repositoryLocation = workspaceProvider.resolveRepositoryLocation(
                        validatedRequest.getRepositoryUri());

        /*
         * Stage 4
         * Clone or update repository.
         */
        repositoryCloner.load(validatedRequest.getRepositoryUri(), repositoryLocation);

        /*
         * Stage 5
         * Extract repository metadata.
         */
        RepositoryMetadata metadata = metadataExtractor.extract(repositoryLocation);

        /*
         * Stage 6
         * Build compiler context.
         */
        return new CompilerContext(metadata);
    }
}