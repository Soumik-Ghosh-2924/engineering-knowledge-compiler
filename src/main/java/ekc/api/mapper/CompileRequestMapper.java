package ekc.api.mapper;

import ekc.api.request.CompileRequest;
import ekc.shared.model.acquisition.CompileRepositoryRequest;

/**
 * Maps REST API requests into immutable compiler domain models.
 */
public interface CompileRequestMapper {

    /**
     * Converts the transport-layer request into the compiler domain model.
     * @param request API request
     * @return immutable compiler domain request
     */
    CompileRepositoryRequest toDomain(CompileRequest request);
}