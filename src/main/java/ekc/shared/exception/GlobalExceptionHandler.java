package ekc.shared.exception;

import ekc.api.response.CompileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts compiler exceptions into REST responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRepositoryException.class)
    public ResponseEntity<CompileResponse> handleInvalidRepository(
            InvalidRepositoryException exception) {

        return ResponseEntity
                .badRequest()
                .body(new CompileResponse(
                        "FAILED",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(WorkspaceInitializationException.class)
    public ResponseEntity<CompileResponse> handleWorkspace(
            WorkspaceInitializationException exception) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CompileResponse(
                        "FAILED",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CompileResponse> handleUnexpected(
            Exception exception) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CompileResponse(
                        "FAILED",
                        "Unexpected compiler error."
                ));
    }
}