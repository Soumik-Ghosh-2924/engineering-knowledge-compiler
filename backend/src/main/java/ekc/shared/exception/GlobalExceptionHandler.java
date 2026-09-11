package ekc.shared.exception;

import ekc.api.response.CompileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.NoSuchElementException;

/**
 * Converts compiler exceptions into REST responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CompileResponse> handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("The analysis request is invalid.");

        return ResponseEntity.badRequest().body(new CompileResponse("FAILED", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CompileResponse> handleMalformedRequest(
            IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new CompileResponse("FAILED", exception.getMessage()));
    }

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

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<CompileResponse> handleNotFound(NoSuchElementException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new CompileResponse("FAILED", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CompileResponse> handleConflict(IllegalStateException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new CompileResponse("FAILED", exception.getMessage()));
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
