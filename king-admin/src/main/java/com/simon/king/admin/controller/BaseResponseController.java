package com.simon.king.admin.controller;

import org.springframework.http.ResponseEntity;

/**
 * @author robot
 */
public abstract class BaseResponseController {
    <T> ResponseEntity<T> ok(T body){
        return ResponseEntity.ok().body(body);
    }
}
