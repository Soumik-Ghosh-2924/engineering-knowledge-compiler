package ekc.compiler;

import ekc.compiler.ast.AstExtractor;
import ekc.compiler.ast.AstParser;
import ekc.compiler.repository.RepositoryLoader;
import ekc.compiler.source.SourceDiscoveryService;
import ekc.shared.model.acquisition.CompileRepositoryRequest;
import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.analysis.CompilationResult;
import ekc.shared.model.analysis.CompilationSummary;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class CompilerEngineImpl implements CompilerEngine {

    private final RepositoryLoader repositoryLoader;
    private final SourceDiscoveryService sourceDiscoveryService;
    private final AstParser astParser;
    private final AstExtractor astExtractor;
    private final Clock clock;

    public CompilerEngineImpl(
            RepositoryLoader repositoryLoader,
            SourceDiscoveryService sourceDiscoveryService,
            AstParser astParser,
            AstExtractor astExtractor) {
        this(repositoryLoader, sourceDiscoveryService, astParser, astExtractor, Clock.systemUTC());
    }

    CompilerEngineImpl(
            RepositoryLoader repositoryLoader,
            SourceDiscoveryService sourceDiscoveryService,
            AstParser astParser,
            AstExtractor astExtractor,
            Clock clock) {
        this.repositoryLoader = repositoryLoader;
        this.sourceDiscoveryService = sourceDiscoveryService;
        this.astParser = astParser;
        this.astExtractor = astExtractor;
        this.clock = clock;
    }

    @Override
    public CompilationResult analyze(CompileRepositoryRequest request) {
        long startedAt = clock.millis();

        CompilerContext context = repositoryLoader.load(request);
        context = sourceDiscoveryService.discover(context);
        context = astParser.parse(context);
        context = astExtractor.extract(context);

        CompilationSummary summary = CompilationSummary.from(context);
        long durationMs = Math.max(0, clock.millis() - startedAt);

        return new CompilationResult(
                "ANALYZED",
                "Repository analyzed successfully. No target build was executed.",
                durationMs,
                summary);
    }
}
