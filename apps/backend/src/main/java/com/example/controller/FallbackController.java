package com.example.controller;

import com.example.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处理所有未匹配的请求，返回 404
 */
@RestController
public class FallbackController {

    @RequestMapping("/**")
    public ResponseEntity<Result<Void>> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.fail(404, "接口不存在"));
    }
}
