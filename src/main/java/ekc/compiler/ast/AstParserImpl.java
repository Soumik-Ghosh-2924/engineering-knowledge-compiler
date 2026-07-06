package ekc.compiler.ast;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import ekc.shared.exception.AstParserException;
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
        RepositoryAst repositoryAst = parseRepository(repositorySource);

        return new CompilerContext(repositoryMetadata, repositorySource, repositoryAst, null);
    }


    private RepositoryAst parseRepository(RepositorySource repositorySource) {
        List<ParsedSourceFile> parsedFiles = new ArrayList<>();
        for (SourceFile sourceFile : repositorySource.getSourceFiles()) {
            parsedFiles.add(parseSourceFile(sourceFile));
        }
        return new RepositoryAst(parsedFiles);
    }


    private ParsedSourceFile parseSourceFile(SourceFile sourceFile) {
        try {
            String source = Files.readString(sourceFile.getPath());
            ParseResult<CompilationUnit> result = javaParser.parse(source);
            CompilationUnit compilationUnit =
                    result.getResult().orElseThrow(() ->
                                    new AstParserException(
                                            "Failed to parse source file: "
                                                    + sourceFile.getRelativePath()));
            return new ParsedSourceFile(sourceFile, compilationUnit, null);
        } catch (IOException exception) {

            throw new AstParserException("Unable to read source file: " + sourceFile.getRelativePath(), exception);
        }
    }
}