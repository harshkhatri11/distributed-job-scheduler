package com.harshkhatri.worker.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyChecker implements IdempotencyChecker {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "processed:execution:";

    @Override
    public boolean markProcessingIfAbsent(String executionId, Duration ttl) {
        Boolean firstTime = redisTemplate.opsForValue()
                .setIfAbsent(PREFIX + executionId, "1", ttl);
        return Boolean.TRUE.equals(firstTime);
    }
}
