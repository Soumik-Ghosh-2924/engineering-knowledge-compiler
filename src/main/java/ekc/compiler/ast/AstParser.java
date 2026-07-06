package ekc.compiler.ast;

import ekc.shared.model.acquisition.CompilerContext;

/**
 * Parses all discovered source files into their corresponding
 * Abstract Syntax Trees (AST).
 * This represents Stage 2.2 of the compiler pipeline.
 */
public interface AstParser {

    /**
     * Parses every discovered source file contained within the
     * supplied compiler context and enriches the context with
     * the resulting repository AST.
     * @param context compiler context
     * @return enriched compiler context
     */
    CompilerContext parse(CompilerContext context);
}