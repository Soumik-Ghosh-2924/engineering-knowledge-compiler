package ekc.shared.model.ast;

import com.github.javaparser.ast.CompilationUnit;
import ekc.shared.model.source.SourceFile;

import java.util.Objects;

/**
 * Represents a successfully parsed source file.
 * Combines the original SourceFile with its corresponding
 * Abstract Syntax Tree (CompilationUnit).
 */
public final class ParsedSourceFile {

    private final SourceFile sourceFile;
    private final CompilationUnit compilationUnit;
    private final ParsedCompilationUnit parsedCompilationUnit;

    public ParsedSourceFile(SourceFile sourceFile, CompilationUnit compilationUnit, ParsedCompilationUnit parsedCompilationUnit) {
        this.sourceFile = Objects.requireNonNull(sourceFile);
        this.compilationUnit = Objects.requireNonNull(compilationUnit);
        this.parsedCompilationUnit = parsedCompilationUnit;
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public ParsedCompilationUnit getParsedCompilationUnit() {
        return parsedCompilationUnit;
    }

    @Override
    public String toString() {

        return "ParsedSourceFile{" +
                "sourceFile=" + sourceFile.getRelativePath() +
                '}';
    }
}