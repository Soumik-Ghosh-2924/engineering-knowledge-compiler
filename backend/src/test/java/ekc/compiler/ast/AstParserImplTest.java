package ekc.compiler.ast;

import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.acquisition.RepositoryMetadata;
import ekc.shared.model.analysis.AnalysisStage;
import ekc.shared.model.analysis.DiagnosticSeverity;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.source.SourceFile;
import ekc.shared.model.source.SourceLanguage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AstParserImplTest {

    @TempDir
    Path repositoryRoot;

    @Test
    void recordsAFileDiagnosticAndContinuesParsingOtherSources() throws Exception {
        Path validFile = repositoryRoot.resolve("Example.java");
        Files.writeString(validFile, "package sample; public class Example { void run() {} }");
        Path missingFile = repositoryRoot.resolve("Missing.java");

        RepositorySource source = new RepositorySource(
                List.of(
                        new SourceFile(validFile, "Example.java", SourceLanguage.JAVA),
                        new SourceFile(missingFile, "Missing.java", SourceLanguage.JAVA)),
                Map.of(SourceLanguage.JAVA, 2));
        CompilerContext context = new CompilerContext(
                new RepositoryMetadata("fixture", "main", repositoryRoot),
                source,
                null,
                null);

        CompilerContext result = new AstParserImpl().parse(context);

        assertThat(result.getRepositoryAst().getTotalParsedFiles()).isEqualTo(1);
        assertThat(result.getDiagnostics()).singleElement().satisfies(diagnostic -> {
            assertThat(diagnostic.getStage()).isEqualTo(AnalysisStage.AST_PARSING);
            assertThat(diagnostic.getSeverity()).isEqualTo(DiagnosticSeverity.ERROR);
            assertThat(diagnostic.getSourcePath()).isEqualTo("Missing.java");
        });
    }

    @Test
    void recordsJavaParserProblemsInsteadOfDiscardingThem() throws Exception {
        Path invalidFile = repositoryRoot.resolve("Broken.java");
        Files.writeString(invalidFile, "package sample; public class Broken { void run( }");
        RepositorySource source = new RepositorySource(
                List.of(new SourceFile(invalidFile, "Broken.java", SourceLanguage.JAVA)),
                Map.of(SourceLanguage.JAVA, 1));
        CompilerContext context = new CompilerContext(
                new RepositoryMetadata("fixture", "main", repositoryRoot),
                source,
                null,
                null);

        CompilerContext result = new AstParserImpl().parse(context);

        assertThat(result.getDiagnostics()).isNotEmpty();
        assertThat(result.getDiagnostics())
                .allMatch(diagnostic -> diagnostic.getStage() == AnalysisStage.AST_PARSING);
    }
}
