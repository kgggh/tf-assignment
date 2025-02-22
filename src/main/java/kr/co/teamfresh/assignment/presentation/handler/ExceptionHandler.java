package kr.co.teamfresh.assignment.presentation.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handle(RuntimeException e) {
        log.error(e.getMessage(), e);

        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception e) {
        log.error(e.getMessage(), e);

        return ResponseEntity.internalServerError().body(new ErrorResponse("Internal Server Error"));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        List<ErrorResponse.InvalidInputs> invalidInputs = fieldErrors.stream()
            .map(error -> new ErrorResponse.InvalidInputs(error.getField(), error.getDefaultMessage()))
            .toList();

        return ResponseEntity.badRequest().body(new ErrorResponse("Bad Request", invalidInputs));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(String message, List<InvalidInputs> invalidInputs) {
        public ErrorResponse(String message) {
            this(message, null);
        }

        public record InvalidInputs(String fieldName, String message) {
        }
    }
}
