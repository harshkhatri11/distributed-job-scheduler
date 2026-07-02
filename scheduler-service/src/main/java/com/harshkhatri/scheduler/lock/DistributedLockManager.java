package com.harshkhatri.scheduler.lock;

import java.time.Duration;

public interface DistributedLockManager {
    /** Returns a unique ownership token if acquired, or null if lock already held. */
    String tryAcquireLock(String key, Duration ttl);

    /** Releases the lock only if the caller still owns it. Returns true if released. */
    boolean releaseLock(String key, String token);
}
