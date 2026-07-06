package ekc.api.controller;

import ekc.api.mapper.CompileRequestMapper;
import ekc.api.request.CompileRequest;
import ekc.api.response.CompileResponse;
import ekc.compiler.ast.AstExtractor;
import ekc.compiler.ast.AstParser;
import ekc.compiler.repository.RepositoryLoader;
import ekc.compiler.source.SourceDiscoveryService;
import ekc.shared.model.acquisition.CompilerContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Entry point into the Engineering Knowledge Compiler.
 */
@RestController
@RequestMapping("/api/v1/compiler")
public class CompilerController {

    private final RepositoryLoader repositoryLoader;
    private final CompileRequestMapper compileRequestMapper;
    private final SourceDiscoveryService sourceDiscoveryService;
    private final AstParser astParser;
    private final AstExtractor astExtractor;

    public CompilerController(
            RepositoryLoader repositoryLoader,
            SourceDiscoveryService sourceDiscoveryService,
            AstParser astParser,
            AstExtractor astExtractor,
            CompileRequestMapper compileRequestMapper) {

        this.repositoryLoader = repositoryLoader;
        this.sourceDiscoveryService = sourceDiscoveryService;
        this.astParser = astParser;
        this.astExtractor = astExtractor;
        this.compileRequestMapper = compileRequestMapper;
    }

    @PostMapping("/compile")
    public ResponseEntity<CompileResponse> compile(
            @Valid @RequestBody CompileRequest request) {

        CompilerContext context =
                repositoryLoader.load(
                        compileRequestMapper.toDomain(request));

        context =
                sourceDiscoveryService.discover(context);

        context =
                astParser.parse(context);

        context =
                astExtractor.extract(context);

        return ResponseEntity
                .accepted()
                .body(new CompileResponse(
                        "ACCEPTED",
                        "Repository compiled successfully."
                ));
    }
}