package org.example.hrsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        //return HttpStatus.NOT_FOUND status with the error message in the body
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
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
}
