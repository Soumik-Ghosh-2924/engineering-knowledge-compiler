package ekc.compiler.repository;

import ekc.shared.model.CompileRepositoryRequest;
import ekc.shared.model.CompilerContext;

/**
 * Entry point into the Repository Acquisition stage
 * of the Engineering Knowledge Compiler.
 */
public interface RepositoryLoader {

    /**
     * Loads a repository into the compiler workspace.
     *
     * @param request compile request
     * @return compiler context
     */
    CompilerContext load(
            CompileRepositoryRequest request
    );
}