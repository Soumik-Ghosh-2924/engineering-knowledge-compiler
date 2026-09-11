package ekc.api.v2.controller;

import ekc.api.v2.request.CreateWorkspaceRequest;
import ekc.api.v2.response.WorkspaceAnalysisResponse;
import ekc.api.v2.response.WorkspaceResponse;
import ekc.shared.config.Phase2FeatureProperties;
import ekc.workspace.WorkspaceService;
import ekc.workspace.analysis.WorkspaceAnalysis;
import ekc.workspace.analysis.WorkspaceAnalysisService;
import ekc.workspace.analysis.WorkspaceOverviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2")
public class WorkspaceController {
    private final Phase2FeatureProperties featureProperties;
    private final WorkspaceService workspaceService;
    private final WorkspaceAnalysisService analysisService;
    private final WorkspaceOverviewService overviewService;

    public WorkspaceController(
            Phase2FeatureProperties featureProperties,
            WorkspaceService workspaceService,
            WorkspaceAnalysisService analysisService,
            WorkspaceOverviewService overviewService) {
        this.featureProperties = featureProperties;
        this.workspaceService = workspaceService;
        this.analysisService = analysisService;
        this.overviewService = overviewService;
    }

    @PostMapping("/workspaces")
    public ResponseEntity<?> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        if (!featureProperties.isEnabled()) return ResponseEntity.notFound().build();
        WorkspaceResponse response = WorkspaceResponse.from(workspaceService.create(request));
        return ResponseEntity.created(URI.create("/api/v2/workspaces/" + response.id())).body(response);
    }

    @PostMapping("/workspaces/{workspaceId}/analyse")
    public ResponseEntity<?> startAnalysis(@PathVariable UUID workspaceId) {
        if (!featureProperties.isEnabled()) return ResponseEntity.notFound().build();
        WorkspaceAnalysis analysis = analysisService.start(workspaceId);
        return ResponseEntity.accepted().body(WorkspaceAnalysisResponse.from(analysis));
    }

    @GetMapping("/analyses/{analysisId}")
    public ResponseEntity<?> getAnalysis(@PathVariable UUID analysisId) {
        if (!featureProperties.isEnabled()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(WorkspaceAnalysisResponse.from(analysisService.get(analysisId)));
    }

    @GetMapping("/analyses/{analysisId}/overview")
    public ResponseEntity<?> getOverview(@PathVariable UUID analysisId) {
        if (!featureProperties.isEnabled()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(overviewService.create(analysisId));
    }
}
