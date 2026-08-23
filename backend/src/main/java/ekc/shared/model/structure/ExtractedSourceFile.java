package ekc.shared.model.structure;

import ekc.shared.model.ast.ParsedCompilationUnit;
import ekc.shared.model.ast.ParsedSourceFile;

import java.util.Objects;

/**
 * Represents the compiler's extracted understanding
 * of a single Java source file.
 * This model links the parser output with the
 * compiler's language model.
 */
public final class ExtractedSourceFile {

    private final ParsedSourceFile parsedSourceFile;

    private final ParsedCompilationUnit parsedCompilationUnit;

    public ExtractedSourceFile(
            ParsedSourceFile parsedSourceFile,
            ParsedCompilationUnit parsedCompilationUnit) {

        this.parsedSourceFile =
                Objects.requireNonNull(parsedSourceFile);

        this.parsedCompilationUnit =
                Objects.requireNonNull(parsedCompilationUnit);
    }

    public ParsedSourceFile getParsedSourceFile() {
        return parsedSourceFile;
    }

    public ParsedCompilationUnit getParsedCompilationUnit() {
        return parsedCompilationUnit;
    }

    @Override
    public String toString() {
        return "ExtractedSourceFile{" +
                "sourceFile=" +
                parsedSourceFile.getSourceFile().getRelativePath() +
                '}';
    }
}