package ekc.api.controller;

import ekc.api.mapper.CompileRequestMapper;
import ekc.api.request.CompileRequest;
import ekc.api.response.CompileResponse;
import ekc.compiler.CompilerEngine;
import ekc.shared.model.analysis.CompilationResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Entry point into the Engineering Knowledge Compiler.
 */
@RestController
@RequestMapping("/api/v1/compiler")
public class CompilerController {

    private final CompilerEngine compilerEngine;
    private final CompileRequestMapper compileRequestMapper;

    public CompilerController(
            CompilerEngine compilerEngine,
            CompileRequestMapper compileRequestMapper) {

        this.compilerEngine = compilerEngine;
        this.compileRequestMapper = compileRequestMapper;
    }

    /**
     * Performs static repository analysis. The submitted repository's own
     * Maven/Gradle build is not executed.
     *
     * /compile remains temporarily available for existing clients.
     */
    @PostMapping({"/analyze", "/compile"})
    public ResponseEntity<CompileResponse> analyze(
            @Valid @RequestBody CompileRequest request) {

        CompilationResult result = compilerEngine.analyze(
                compileRequestMapper.toDomain(request));

        return ResponseEntity.ok(new CompileResponse(result));
    }
}
