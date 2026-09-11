package ekc.workspace;

import ekc.api.v2.request.CreateWorkspaceRequest;
import ekc.api.v2.request.WorkspaceRepositoryRequest;
import ekc.compiler.repository.RepositoryValidator;
import ekc.shared.model.acquisition.CompileRepositoryRequest;
import ekc.workspace.model.Workspace;
import ekc.workspace.model.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkspaceService {
    private final RepositoryValidator repositoryValidator;
    private final ConcurrentHashMap<UUID, Workspace> workspaces = new ConcurrentHashMap<>();
    private final Clock clock;

    @Autowired
    public WorkspaceService(RepositoryValidator repositoryValidator) {
        this(repositoryValidator, Clock.systemUTC());
    }

    WorkspaceService(RepositoryValidator repositoryValidator, Clock clock) {
        this.repositoryValidator = repositoryValidator;
        this.clock = clock;
    }

    public Workspace create(CreateWorkspaceRequest request) {
        long primaryCount = request.repositories().stream().filter(WorkspaceRepositoryRequest::primary).count();
        if (primaryCount != 1) {
            throw new IllegalArgumentException("Exactly one repository must be marked as primary.");
        }
        List<WorkspaceRepository> repositories = request.repositories().stream()
                .map(this::createRepository)
                .toList();
        Workspace workspace = new Workspace(UUID.randomUUID(), request.name().trim(), repositories, Instant.now(clock));
        workspaces.put(workspace.id(), workspace);
        return workspace;
    }

    public Workspace get(UUID workspaceId) {
        Workspace workspace = workspaces.get(workspaceId);
        if (workspace == null) {
            throw new NoSuchElementException("Workspace not found: " + workspaceId);
        }
        return workspace;
    }

    private WorkspaceRepository createRepository(WorkspaceRepositoryRequest request) {
        URI uri = URI.create(request.repositoryUrl().trim());
        repositoryValidator.validate(new CompileRepositoryRequest(uri));
        return new WorkspaceRepository(
                UUID.randomUUID(), uri, request.role(), request.primary(),
                normalize(request.baseRef()), normalize(request.headRef()), normalize(request.declaredPurpose()));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
