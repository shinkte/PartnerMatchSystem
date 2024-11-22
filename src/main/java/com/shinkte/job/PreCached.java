package com.shinkte.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shinkte.mapper.UserMapper;
import com.shinkte.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: shinkte
 * @Description: 缓存预热
 * @CreateTime: 2024-11-11
 */
@Component
@Slf4j
public class PreCached {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;  //redison提供看门狗机制，防止缓存击穿不用自动续期

    //重点用户
    private List<Long> mianUserList = Arrays.asList(1L);
    @Scheduled(cron = "0 0 0 * * ?")
    public void doPreCachedRecommend() {
        //增加锁
        RLock lock = redissonClient.getLock("shinkte:precachedjob:doPreCachedRecommend:lock");
        try {
            //只有一个线程可以执行
            //tryLock实现方式使用lua脚本，保证原子性
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)) {
                for(Long userId:mianUserList){
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userMapper.selectPage(new Page<>(1, 20), queryWrapper);
                    String REDIS_KEY = String.format("shinkte:user:recommend:%s", userId);
                    //缓存用户推荐列表
                    try{
                        redisTemplate.opsForValue().set(REDIS_KEY, userPage,30000, TimeUnit.MICROSECONDS);
                    }catch (Exception e){
                        log.error("缓存用户推荐列表失败", e);
                    }
                }
                //释放锁,不能直接释放锁，可能会释放别人的锁
                //lock.unlock();
                lock.unlock();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            //确保最后释放锁，因为可能有多个线程等待锁且只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
