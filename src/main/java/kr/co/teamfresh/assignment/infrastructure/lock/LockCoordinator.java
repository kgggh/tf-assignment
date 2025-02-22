package kr.co.teamfresh.assignment.infrastructure.lock;

public interface LockCoordinator {
    void lock(String key);
    void unlock(String key);
}
