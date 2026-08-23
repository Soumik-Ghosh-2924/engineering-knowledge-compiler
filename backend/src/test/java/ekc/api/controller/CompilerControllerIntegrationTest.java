package ekc.api.controller;

import ekc.api.mapper.CompileRequestMapper;
import ekc.compiler.CompilerEngine;
import ekc.shared.model.acquisition.CompileRepositoryRequest;
import ekc.shared.model.acquisition.CompilerContext;
import ekc.shared.model.acquisition.RepositoryMetadata;
import ekc.shared.model.analysis.CompilationResult;
import ekc.shared.model.analysis.CompilationSummary;
import ekc.shared.model.ast.RepositoryAst;
import ekc.shared.model.source.RepositorySource;
import ekc.shared.model.structure.RepositoryStructure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompilerController.class)
class CompilerControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CompilerEngine compilerEngine;

    @MockBean
    CompileRequestMapper compileRequestMapper;

    @Test
    void returnsStructuredRepositoryAnalysis() throws Exception {
        CompileRepositoryRequest request = new CompileRepositoryRequest(
                URI.create("https://github.com/acme/example.git"));
        CompilerContext context = new CompilerContext(
                new RepositoryMetadata("example", "main", Path.of("/tmp/example")),
                new RepositorySource(List.of(), Map.of()),
                new RepositoryAst(List.of()),
                new RepositoryStructure(List.of()));
        CompilationResult result = new CompilationResult(
                "ANALYZED",
                "Repository analyzed successfully. No target build was executed.",
                42,
                CompilationSummary.from(context));

        when(compileRequestMapper.toDomain(any())).thenReturn(request);
        when(compilerEngine.analyze(request)).thenReturn(result);

        mockMvc.perform(post("/api/v1/compiler/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryUrl\":\"https://github.com/acme/example.git\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANALYZED"))
                .andExpect(jsonPath("$.durationMs").value(42))
                .andExpect(jsonPath("$.analysis.repositoryName").value("example"))
                .andExpect(jsonPath("$.analysis.sourceFiles").value(0))
                .andExpect(jsonPath("$.analysis.diagnostics").isArray());
    }

    @Test
    void rejectsBlankRepositoryUrls() throws Exception {
        mockMvc.perform(post("/api/v1/compiler/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryUrl\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
