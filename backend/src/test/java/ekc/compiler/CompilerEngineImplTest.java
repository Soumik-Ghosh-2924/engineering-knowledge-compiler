package ekc.compiler;

import ekc.compiler.ast.AstExtractor;
import ekc.compiler.ast.AstParser;
import ekc.compiler.repository.RepositoryLoader;
import ekc.compiler.source.SourceDiscoveryService;
import ekc.shared.model.acquisition.CompileRepositoryRequest;
import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.acquisition.RepositoryMetadata;
import ekc.shared.model.ast.RepositoryAst;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.structure.RepositoryStructure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;

import java.net.URI;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompilerEngineImplTest {

    @TempDir
    Path repositoryRoot;

    @Test
    void ownsThePipelineOrderAndReturnsAStructuredResult() {
        RepositoryLoader loader = mock(RepositoryLoader.class);
        SourceDiscoveryService discovery = mock(SourceDiscoveryService.class);
        AstParser parser = mock(AstParser.class);
        AstExtractor extractor = mock(AstExtractor.class);
        CompileRepositoryRequest request = new CompileRepositoryRequest(
                URI.create("https://github.com/acme/example.git"));

        RepositoryMetadata metadata = new RepositoryMetadata("example", "main", repositoryRoot);
        CompilerContext acquired = new CompilerContext(metadata, null, null, null);
        CompilerContext discovered = acquired.withRepositorySource(new RepositorySource(List.of(), Map.of()));
        CompilerContext parsed = discovered.withRepositoryAst(new RepositoryAst(List.of()), List.of());
        CompilerContext extracted = parsed.withRepositoryStructure(new RepositoryStructure(List.of()), List.of());

        when(loader.load(request)).thenReturn(acquired);
        when(discovery.discover(acquired)).thenReturn(discovered);
        when(parser.parse(discovered)).thenReturn(parsed);
        when(extractor.extract(parsed)).thenReturn(extracted);

        CompilerEngineImpl engine = new CompilerEngineImpl(
                loader,
                discovery,
                parser,
                extractor,
                Clock.fixed(Instant.parse("2026-08-23T00:00:00Z"), ZoneOffset.UTC));

        var result = engine.analyze(request);

        assertThat(result.getStatus()).isEqualTo("ANALYZED");
        assertThat(result.getMessage()).contains("No target build was executed");
        assertThat(result.getSummary().getRepositoryName()).isEqualTo("example");
        assertThat(result.getSummary().getSourceFiles()).isZero();

        InOrder order = inOrder(loader, discovery, parser, extractor);
        order.verify(loader).load(request);
        order.verify(discovery).discover(acquired);
        order.verify(parser).parse(discovered);
        order.verify(extractor).extract(parsed);
    }
}
