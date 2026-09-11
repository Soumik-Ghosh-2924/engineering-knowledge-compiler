package ekc.api.v2.controller;

import ekc.shared.config.Phase2FeatureProperties;
import ekc.workspace.WorkspaceService;
import ekc.workspace.analysis.WorkspaceAnalysisService;
import ekc.workspace.analysis.WorkspaceAnalysis;
import ekc.workspace.analysis.WorkspaceAnalysisStatus;
import ekc.workspace.analysis.WorkspaceOverviewService;
import ekc.workspace.model.RepositoryRole;
import ekc.workspace.model.Workspace;
import ekc.workspace.model.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkspaceController.class)
class WorkspaceControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    Phase2FeatureProperties featureProperties;
    @MockBean
    WorkspaceService workspaceService;
    @MockBean
    WorkspaceAnalysisService analysisService;
    @MockBean
    WorkspaceOverviewService overviewService;

    @Test
    void hidesV2WhenTheFeatureIsDisabled() throws Exception {
        when(featureProperties.isEnabled()).thenReturn(false);

        mockMvc.perform(post("/api/v2/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validWorkspaceJson()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createsAWorkspaceWhenTheFeatureIsEnabled() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID repositoryId = UUID.randomUUID();
        Workspace workspace = new Workspace(
                workspaceId,
                "EKC system",
                List.of(new WorkspaceRepository(
                        repositoryId,
                        URI.create("https://github.com/acme/app.git"),
                        RepositoryRole.APPLICATION,
                        true,
                        "main",
                        null,
                        "Analyzes repositories")),
                Instant.parse("2026-09-11T00:00:00Z"));
        when(featureProperties.isEnabled()).thenReturn(true);
        when(workspaceService.create(any())).thenReturn(workspace);

        mockMvc.perform(post("/api/v2/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validWorkspaceJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(workspaceId.toString()))
                .andExpect(jsonPath("$.repositories[0].id").value(repositoryId.toString()))
                .andExpect(jsonPath("$.repositories[0].role").value("APPLICATION"));
    }

    @Test
    void startsAnalysisUsingThePublishedV2Contract() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();
        when(featureProperties.isEnabled()).thenReturn(true);
        when(analysisService.start(workspaceId)).thenReturn(new WorkspaceAnalysis(
                analysisId,
                workspaceId,
                WorkspaceAnalysisStatus.QUEUED,
                Instant.parse("2026-09-11T00:00:00Z"),
                null,
                List.of()));

        mockMvc.perform(post("/api/v2/workspaces/{workspaceId}/analyse", workspaceId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(analysisId.toString()))
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    private String validWorkspaceJson() {
        return """
                {
                  "name": "EKC system",
                  "repositories": [{
                    "repositoryUrl": "https://github.com/acme/app.git",
                    "role": "APPLICATION",
                    "primary": true,
                    "baseRef": "main",
                    "declaredPurpose": "Analyzes repositories"
                  }]
                }
                """;
    }
}
