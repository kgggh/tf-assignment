package kr.co.teamfresh.assignment.infrastructure.lock.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class RedisLockCoordinatorTest {
    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @InjectMocks
    private RedisLockCoordinator redisLockCoordinator;

    @BeforeEach
    void init() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
    }

    private static final String LOCK_KEY = "test:1";

    @Test
    void 락을_정상적으로_획득한다() throws InterruptedException {
        //given
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);

        //when
        //then
        assertThatCode(() -> redisLockCoordinator.lock(LOCK_KEY))
            .doesNotThrowAnyException();

        verify(rLock, times(1)).tryLock(10, 30, TimeUnit.SECONDS);
    }

    @Test
    void 다른_쓰레드가_소유하고_있을시_락_획득에_실패한다() throws InterruptedException {
        //given
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(false);

        //when
        //then
        assertThatThrownBy(() -> redisLockCoordinator.lock(LOCK_KEY))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("락 획득 실패 - key: " + LOCK_KEY);

        verify(rLock, times(1)).tryLock(10, 30, TimeUnit.SECONDS);
    }

    @Test
    void 락_획득_중_인터럽트가_발생하면_예외를_발생시킨다() throws InterruptedException {
        //given
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        //when
        //then
        assertThatThrownBy(() -> redisLockCoordinator.lock(LOCK_KEY))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("인터럽트 발생");

        verify(rLock, times(1)).tryLock(10, 30, TimeUnit.SECONDS);
        
    }

    @Test
    void 락_해제에_성공한다() {
        //given
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        //when
        redisLockCoordinator.unlock(LOCK_KEY);

        //then
        verify(rLock, times(1)).unlock();
    }

    @Test
    void 현재_스레드가_락을_보유하고_있지_않으면_해제하지_않는다() {
        //given
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        //when
        redisLockCoordinator.unlock(LOCK_KEY);

        //then
        verify(rLock, times(0)).unlock();
    }

    @Test
    void 멀티스레드_환경에서_하나의_쓰레드만_락을_획득한다() throws InterruptedException {
        //given
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true, false);

        var thread1 = new Thread(() -> redisLockCoordinator.lock(LOCK_KEY));
        var thread2 = new Thread(() -> {
            assertThatThrownBy(() -> redisLockCoordinator.lock(LOCK_KEY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("락 획득 실패 - key: " + LOCK_KEY);
        });

        //when
        thread1.start();
        thread1.join();
        thread2.start();
        thread2.join();

        //then
        verify(rLock, times(2)).tryLock(10, 30, TimeUnit.SECONDS);
    }
}
