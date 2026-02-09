package org.example.hrsystem.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gravity9.jsonpatch.JsonPatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        //return HttpStatus.NOT_FOUND status with the error message in the body
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
        //return  HttpStatus.CONFLICT status with the error message in the body
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        //return HttpStatus.BAD_REQUEST status with the error message in the body
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    //validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String> > handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors=new ArrayList<>();
        for (int i = 0; i <  ex.getBindingResult().getFieldErrors().size(); i++) {
                errors.add(ex.getBindingResult().getFieldErrors().get(i).getDefaultMessage());
        }
        //
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler({ JsonPatchException.class, JsonProcessingException.class })
    public ResponseEntity<String> handlePatchExceptions(Exception ex) {
        return ResponseEntity.badRequest().body("Patch error: " + ex.getMessage());
    }
}
