package kr.co.teamfresh.assignment.infrastructure.lock.redis;

import kr.co.teamfresh.assignment.infrastructure.lock.LockCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisLockCoordinator implements LockCoordinator {
    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:";
    private static final int DEFAULT_LOCK_WAIT_TIME = 10;
    private static final int DEFAULT_LOCK_HOLD_TIME = 30;

    @Override
    public void lock(String key) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);

        try {
            if(!lock.tryLock(DEFAULT_LOCK_WAIT_TIME, DEFAULT_LOCK_HOLD_TIME, TimeUnit.SECONDS)) {
                log.warn("락 획득 실패 - key: {}", key);
                throw new IllegalStateException("락 획득 실패 - key: " + key);
            }

            log.info("락 획득 완료 - key:{}", key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생 " + key, e);
        }
    }

    @Override
    public void multipleLock(List<String> keys) {
        List<RLock> locks = keys.stream()
            .map(redissonClient::getLock)
            .toList();

        RLock lock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));

        try {
            if(!lock.tryLock(DEFAULT_LOCK_WAIT_TIME, DEFAULT_LOCK_HOLD_TIME, TimeUnit.SECONDS)) {
                log.warn("락 획득 실패 - keys: {}", keys);
                throw new IllegalStateException("락 획득 실패 - key: " + keys);
            }

            log.info("락 획득 완료 - keys: {}", keys);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생 " + keys, e);
        }
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);

        if (!lock.isHeldByCurrentThread()) {
            log.warn("현재 락을 보유하고 있지 않음 - key: {}",key);

            return;
        }

        try {
            lock.unlock();
            log.info("락 해제 완료 - key:{}", key);
        } catch (Exception e) {
            log.error("현재 락을 보유하고 있지 않음 - key: {}", key, e);
        }
    }

    @Override
    public void multipleUnlock(List<String> keys) {
        List<RLock> locks = keys.stream()
            .map(redissonClient::getLock)
            .toList();

        RLock lock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));

        if (!lock.isHeldByCurrentThread()) {
            log.warn("현재 락을 보유하고 있지 않음 - keys: {}", keys);

            return;
        }

        try {
            lock.unlock();
            log.info("락 해제 완료 - keys:{}", keys);
        } catch (Exception e) {
            log.error("현재 락을 보유하고 있지 않음 - keys: {}", keys, e);
        }
    }
}
