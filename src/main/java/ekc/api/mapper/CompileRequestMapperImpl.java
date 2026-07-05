package ekc.api.mapper;

import ekc.api.request.CompileRequest;
import ekc.shared.model.CompileRepositoryRequest;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.Objects;

/**
 * Default implementation of the CompileRequestMapper.
 */
@Component
public class CompileRequestMapperImpl implements CompileRequestMapper {
    @Override
    public CompileRepositoryRequest toDomain(CompileRequest request) {
        Objects.requireNonNull(
                request,
                "Compile request cannot be null."
        );
        return new CompileRepositoryRequest(
                URI.create(request.getRepositoryUrl().trim())
        );
    }
}