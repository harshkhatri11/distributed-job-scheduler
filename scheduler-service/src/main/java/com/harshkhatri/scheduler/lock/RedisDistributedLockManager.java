package com.harshkhatri.scheduler.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLockManager implements DistributedLockManager {

    private final StringRedisTemplate redisTemplate;

    // Atomic compare-and-delete: only removes the key if we still own it (token matches).
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                    "  return redis.call('DEL', KEYS[1]) " +
                    "else " +
                    "  return 0 " +
                    "end",
            Long.class
    );

    @Override
    public String tryAcquireLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        return Boolean.TRUE.equals(acquired) ? token : null;
    }

    @Override
    public boolean releaseLock(String key, String token) {
        Long result = redisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(key), token);
        boolean released = result != null && result == 1L;
        if (!released) {
            log.warn("Could not release lock={} — not owner or already expired", key);
        }
        return released;
    }
}
