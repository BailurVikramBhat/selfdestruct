package com.vikrambhat.selfdestruct.controller;

import com.vikrambhat.selfdestruct.service.SecretService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/secrets")
@CrossOrigin(origins = "*")
public class SecretController {
    private final SecretService service;
    public SecretController(SecretService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createSecret(@RequestBody SecretRequest request) {
        String id = service.createSecret(request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));

    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getSecret(@PathVariable String id) {
        String secret = service.getSecret(id);
        if(secret == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "this message was self-destructed, or never existed"));
        }
        return ResponseEntity.ok(Map.of("content", secret));
    }

    public record SecretRequest(String content) {}
}
