package com.centilontech.account.api;

import org.slf4j.MDC; import org.springframework.http.*; import org.springframework.http.converter.HttpMessageNotReadableException; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;
import java.time.Instant; import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class})
    ResponseEntity<Map<String,Object>> badRequest(Exception ex){
        String message=ex instanceof MethodArgumentNotValidException m?m.getBindingResult().getFieldErrors().stream().map(e->e.getField()+" "+e.getDefaultMessage()).findFirst().orElse("Invalid request"):"Malformed request or unknown transaction type";
        return error(HttpStatus.BAD_REQUEST,message);
    }
    private ResponseEntity<Map<String,Object>> error(HttpStatus status,String message){Map<String,Object>b=new LinkedHashMap<>();b.put("timestamp",Instant.now());b.put("status",status.value());b.put("error",status.getReasonPhrase());b.put("message",message);b.put("traceId",MDC.get("traceId"));return ResponseEntity.status(status).body(b);}
}
