package ekc.compiler.ast;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import ekc.shared.model.analysis.AnalysisStage;
import ekc.shared.model.analysis.CompilationDiagnostic;
import ekc.shared.model.analysis.DiagnosticSeverity;
import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.acquisition.RepositoryMetadata;
import ekc.shared.model.ast.ParsedSourceFile;
import ekc.shared.model.ast.RepositoryAst;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.source.SourceFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the AST parser.
 * Responsible for converting every discovered source file
 * into a JavaParser CompilationUnit and constructing the repository-wide AST model.
 */
@Service
public class AstParserImpl implements AstParser {

    private final JavaParser javaParser;

    public AstParserImpl() {
        this.javaParser = new JavaParser();
    }

    @Override
    public CompilerContext parse(CompilerContext context) {

        RepositoryMetadata repositoryMetadata = context.getRepositoryMetadata();
        RepositorySource repositorySource = context.getRepositorySource();
        ParseRepositoryResult result = parseRepository(repositorySource);

        return context.withRepositoryAst(result.repositoryAst(), result.diagnostics());
    }

    private ParseRepositoryResult parseRepository(RepositorySource repositorySource) {
        List<ParsedSourceFile> parsedFiles = new ArrayList<>();
        List<CompilationDiagnostic> diagnostics = new ArrayList<>();
        for (SourceFile sourceFile : repositorySource.getSourceFiles()) {
            ParseSourceResult result = parseSourceFile(sourceFile);
            if (result.parsedSourceFile() != null) {
                parsedFiles.add(result.parsedSourceFile());
            }
            diagnostics.addAll(result.diagnostics());
        }
        return new ParseRepositoryResult(new RepositoryAst(parsedFiles), diagnostics);
    }

    private ParseSourceResult parseSourceFile(SourceFile sourceFile) {
        List<CompilationDiagnostic> diagnostics = new ArrayList<>();
        try {
            String source = Files.readString(sourceFile.getPath());
            ParseResult<CompilationUnit> result = javaParser.parse(source);
            for (Problem problem : result.getProblems()) {
                diagnostics.add(new CompilationDiagnostic(
                        AnalysisStage.AST_PARSING,
                        result.getResult().isPresent()
                                ? DiagnosticSeverity.WARNING
                                : DiagnosticSeverity.ERROR,
                        sourceFile.getRelativePath(),
                        problem.getVerboseMessage()));
            }

            ParsedSourceFile parsedSourceFile = result.getResult()
                    .map(unit -> new ParsedSourceFile(sourceFile, unit, null))
                    .orElse(null);

            if (parsedSourceFile == null && diagnostics.isEmpty()) {
                diagnostics.add(new CompilationDiagnostic(
                        AnalysisStage.AST_PARSING,
                        DiagnosticSeverity.ERROR,
                        sourceFile.getRelativePath(),
                        "JavaParser did not produce a compilation unit."));
            }

            return new ParseSourceResult(parsedSourceFile, diagnostics);
        } catch (IOException | RuntimeException exception) {
            diagnostics.add(new CompilationDiagnostic(
                    AnalysisStage.AST_PARSING,
                    DiagnosticSeverity.ERROR,
                    sourceFile.getRelativePath(),
                    exception.getMessage() == null
                            ? "Unable to parse source file."
                            : exception.getMessage()));
            return new ParseSourceResult(null, diagnostics);
        }
    }

    private record ParseRepositoryResult(
            RepositoryAst repositoryAst,
            List<CompilationDiagnostic> diagnostics) {
    }

    private record ParseSourceResult(
            ParsedSourceFile parsedSourceFile,
            List<CompilationDiagnostic> diagnostics) {
    }
}
