package com.shinkte.Config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: shinkte
 * @Description: Redisson配置客户端
 * @CreateTime: 2024-11-12
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonCondfig {
    private String host;
    private int port;

    @Bean
    public RedissonClient redissonClient() {
        //创建配置对象
        Config config = new Config();
        String redisAddrss=String.format("redis://%s:%d",host,port);
        config.useSingleServer().setAddress(redisAddrss).setDatabase(3);
        //创建实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }

}
