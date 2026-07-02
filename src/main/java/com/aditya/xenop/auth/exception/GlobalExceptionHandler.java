package com.aditya.xenop.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e
    ){
        Map<String , String> errors = new HashMap<>();

        for(FieldError error : e.getBindingResult().getFieldErrors()){
            errors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String , Object> response = new HashMap<>();

        response.put("timestamp" , Instant.now());
        response.put("status" , HttpStatus.BAD_REQUEST.value());
        response.put("error" , "Validation Failed");
        response.put("messages" , errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException e
    ){

        Map<String , Object> response = new HashMap<>();

        response.put("timestamp" , Instant.now());
        response.put("status" , HttpStatus.BAD_REQUEST.value());
        response.put("error" , "Bad Request");
        response.put("messages" , e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({BadCredentialsException.class , UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            Exception e
    ){

        Map<String , Object> response = new HashMap<>();

        response.put("timestamp" , Instant.now());
        response.put("status" , HttpStatus.UNAUTHORIZED.value());
        response.put("error" , "Unauthorized");
        response.put("messages" , "Invalid Email or Password");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception e
    ){

        Map<String , Object> response = new HashMap<>();

        response.put("timestamp" , Instant.now());
        response.put("status" , HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error" , "Internal Server Error");
        response.put("messages" , e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
