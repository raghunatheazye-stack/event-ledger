package com.centilontech.gateway.api;

import com.centilontech.gateway.client.AccountServiceUnavailableException;
import com.centilontech.gateway.service.EventLedgerService.EventNotFoundException;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({ MethodArgumentNotValidException.class, HttpMessageNotReadableException.class })
    ResponseEntity<Map<String, Object>> bad(Exception ex) {
        String message = ex instanceof MethodArgumentNotValidException m
                ? m.getBindingResult().getFieldErrors().stream().map(e -> e.getField() + " " + e.getDefaultMessage())
                        .findFirst().orElse("Invalid request")
                : "Malformed request or unknown event type";
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(EventNotFoundException.class)
    ResponseEntity<Map<String, Object>> missing(EventNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AccountServiceUnavailableException.class)
    ResponseEntity<Map<String, Object>> unavailable(AccountServiceUnavailableException e) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("timestamp", Instant.now());
        b.put("status", status.value());
        b.put("error", status.getReasonPhrase());
        b.put("message", message);
        b.put("traceId", MDC.get("traceId"));
        return ResponseEntity.status(status).body(b);
    }
}
