package ekc.shared.model.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the Abstract Syntax Tree (AST) for an entire repository.
 * This model is the output of the AST Parsing stage and contains
 * all successfully parsed source files.
 */
public final class RepositoryAst {

    private final List<ParsedSourceFile> parsedSourceFiles;
    private final int totalParsedFiles;

    public RepositoryAst(List<ParsedSourceFile> parsedSourceFiles) {
        Objects.requireNonNull(parsedSourceFiles, "Parsed source files cannot be null.");
        this.parsedSourceFiles = List.copyOf(parsedSourceFiles);
        this.totalParsedFiles = parsedSourceFiles.size();
    }

    public List<ParsedSourceFile> getParsedSourceFiles() {
        return Collections.unmodifiableList(parsedSourceFiles);
    }

    public int getTotalParsedFiles() {
        return totalParsedFiles;
    }

    @Override
    public String toString() {
        return "RepositoryAst{" +
                "totalParsedFiles=" + totalParsedFiles +
                '}';
    }
}