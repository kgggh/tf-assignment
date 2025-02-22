package kr.co.teamfresh.assignment.infrastructure.lock.redis;

public class RedisLockAcquisitionException extends RuntimeException {
    public RedisLockAcquisitionException(String message) {
        super(message);
    }

    public RedisLockAcquisitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
