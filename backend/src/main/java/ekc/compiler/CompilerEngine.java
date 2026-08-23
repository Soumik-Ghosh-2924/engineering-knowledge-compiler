package ekc.compiler;

import ekc.shared.model.acquisition.CompileRepositoryRequest;
import ekc.shared.model.analysis.CompilationResult;

/**
 * Owns the complete repository-analysis pipeline.
 */
public interface CompilerEngine {

    CompilationResult analyze(CompileRepositoryRequest request);
}
