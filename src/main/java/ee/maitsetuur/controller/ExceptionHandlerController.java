package ee.maitsetuur.controller;

import ee.maitsetuur.exception.RestaurantAlreadyExistsException;
import ee.maitsetuur.exception.RestaurantNotFoundException;
import ee.maitsetuur.exception.UserAlreadyExistsException;
import ee.maitsetuur.response.ErrorResponse;
import ee.maitsetuur.service.AuthenticationService;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.management.relation.RoleNotFoundException;
import java.time.Instant;
import java.util.ArrayList;

@ControllerAdvice
public class ExceptionHandlerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @ExceptionHandler(RestaurantAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleRestaurantAlreadyExistsException(Exception ex) {
        LOGGER.error("Restaurant already exists: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();

        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(Instant.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRestaurantNotFoundException(Exception ex) {
        LOGGER.error("Restaurant not found: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();

        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setTimestamp(Instant.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(Exception ex) {
        LOGGER.error("User already exists: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse();

        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(Instant.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFoundException(Exception ex) {
        LOGGER.error("Role not found: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse();

        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setTimestamp(Instant.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        LOGGER.error("Constraint error: {}", new ArrayList<>(e.getConstraintViolations()).get(0).getMessageTemplate(), e);
        ErrorResponse error = new ErrorResponse();

        error.setMessage(new ArrayList<>(e.getConstraintViolations()).get(0).getMessageTemplate());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(Instant.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(Exception ex) {
        LOGGER.error("User not found: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();

        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setTimestamp(Instant.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExceptions(Exception ex) {
        LOGGER.error(ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();

        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(Instant.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
