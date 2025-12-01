package com.vikrambhat.selfdestruct.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class SecretService {
    private final StringRedisTemplate redisTemplate;
    public SecretService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public String createSecret(String secretContent) {
        String id = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(id, secretContent, Duration.ofSeconds(60));
        return id;
    }

    public String getSecret(String id) {
        String secret = redisTemplate.opsForValue().get(id);
        if(secret !=null) {
            redisTemplate.delete(id);
        }
        return secret;
    }
}
