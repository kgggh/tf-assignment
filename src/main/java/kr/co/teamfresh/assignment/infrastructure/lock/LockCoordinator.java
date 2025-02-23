package kr.co.teamfresh.assignment.infrastructure.lock;

import java.util.List;

public interface LockCoordinator {
    void lock(String key);
    void multipleLock(List<String> keys);
    void unlock(String key);
    void multipleUnlock(List<String> keys);
}
