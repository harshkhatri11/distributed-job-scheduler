package com.harshkhatri.worker.idempotency;

import java.time.Duration;

public interface IdempotencyChecker {
    /** Returns true if this is the first time we've seen this executionId (safe to process). */
    boolean markProcessingIfAbsent(String executionId, Duration ttl);
}

