package ekc.compiler.source;

import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.source.RepositorySource;

/**
 * Discovers all supported source files within
 * a repository available in the compiler context.
 * This is the entry point for Stage 2.1 of the
 * Engineering Knowledge Compiler pipeline.
 */
public interface SourceDiscoveryService {

    /**
     * Discovers all supported source files contained
     * within the repository represented by the supplied
     * compiler context.
     * @param context compiler context
     * @return repository source model
     */
    CompilerContext discover(CompilerContext context);
}