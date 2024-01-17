package com.developer.linkshortener.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {
    public static ResponseEntity<Object> responseBuilder(
            HttpStatus status, boolean error, String message, Object responseObject
    ){
            Map<String, Object> response = new HashMap<>();
            response.put("status", status);
            response.put("error", error);
            response.put("message", message);
            response.put("data", responseObject);
            return new ResponseEntity<>(response, status);
    }

}
