package ekc.shared.model.analysis;

/**
 * Identifies the compiler stage that produced a diagnostic.
 */
public enum AnalysisStage {
    ACQUISITION,
    SOURCE_DISCOVERY,
    AST_PARSING,
    AST_EXTRACTION
}
