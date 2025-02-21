package kr.co.teamfresh.assignment.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {
    private final RedisProperties redisProperties;
    private static final String ADDRESS_PREFIX = "redis://";

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
            .setAddress(makeAddress());

        return Redisson.create(config);
    }

    private String makeAddress() {
        return ADDRESS_PREFIX + redisProperties.getHost() + ":" + redisProperties.getPort();
    }
}
