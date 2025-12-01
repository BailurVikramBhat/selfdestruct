package com.vikrambhat.selfdestruct.controller;

import com.vikrambhat.selfdestruct.service.RateLimitService;
import com.vikrambhat.selfdestruct.service.SecretService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/secrets")
@CrossOrigin(origins = "*")
public class SecretController {
    private final SecretService service;
    private final RateLimitService rateLimitService;
    public SecretController(SecretService service, RateLimitService rateLimitService) {
        this.service = service;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createSecret(@Valid @RequestBody SecretRequest request, HttpServletRequest servletRequest) {

        String ip = getClientIp(servletRequest);
        Bucket bucket = rateLimitService.resolveBucket(ip);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if(probe.isConsumed()) {
            String id = service.createSecret(request.content());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Rate limit exceeded. Try again in " + waitForRefill + " seconds."));
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getSecret(@PathVariable String id) {
        String secret = service.getSecret(id);
        if(secret == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "this message was self-destructed, or never existed"));
        }
        return ResponseEntity.ok(Map.of("content", secret));
    }

    public record SecretRequest(
            @NotBlank(message = "content cannot be empty")
            @Size(max = 1000, message = "Message is too long! Max 1000 characters.")
            String content) {}

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
