package ekc.compiler.ast;

import ekc.shared.model.acquisition.CompilerContext;

/**
 * Extracts engineering language constructs from the
 * parsed Abstract Syntax Trees.
 *
 * This represents Stage 2.3 of the compiler pipeline.
 */
public interface AstExtractor {

    /**
     * Extracts compiler domain models from the parsed AST
     * and enriches the compiler context.
     * @param context compiler context
     * @return enriched compiler context
     */
    CompilerContext extract(CompilerContext context);

}