package ekc.api.controller;

import ekc.api.mapper.CompileRequestMapper;
import ekc.api.request.CompileRequest;
import ekc.api.response.CompileResponse;
import ekc.shared.model.CompileRepositoryRequest;
import ekc.compiler.repository.RepositoryLoader;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    public CompilerController(
            RepositoryLoader repositoryLoader,
            CompileRequestMapper compileRequestMapper) {

        this.repositoryLoader = repositoryLoader;
        this.compileRequestMapper = compileRequestMapper;
    }

    @PostMapping("/compile")
    public ResponseEntity<CompileResponse> compile(
            @Valid @RequestBody CompileRequest request) {

        repositoryLoader.load(
                compileRequestMapper.toDomain(request)
        );

        return ResponseEntity
                .accepted()
                .body(new CompileResponse(
                        "ACCEPTED",
                        "Repository accepted for compilation."
                ));
    }
}