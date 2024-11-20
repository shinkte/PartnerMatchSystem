/*

package com.shinkte.FirstTasks;

import com.shinkte.Config.RedisTemplateConfig;
import com.shinkte.model.domain.User;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

*/
/**
 * @Author: shinkte
 * @Description: RedisCRUD
 * @CreateTime: 2024-10-29
 * *//*





@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;  //这个类会使用java自带的序列化器，将key和value序列化为字节数组，所以redisTemplate可以直接操作redis中的数据

    @Resource
    private StringRedisTemplate stringRedisTemplate;  //这个类会使用String序列化器，将key和value序列化为字符串，但是仅限于String类型的数据，所以stringRedisTemplate只能操作redis中的字符串数据
   @Resource
   private RedisTemplate redisTemplateConfig;//这个类是自定义的redis配置类，可以自定义redisTemplate的配置，比如设置过期时间，序列化器等等。
*/
/*
    @Test
    public void testRedis() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //列表操作对象ListOperations listOperations = redisTemplate.opsForList();
        valueOperations.set("name", "shinkte");
        valueOperations.set("age", "25");
        valueOperations.set("gender", "male");
        User user =new User();
        user.setId(1L);
        user.setUsername("shinkte");
        valueOperations.set("user", user);

        //查

    }
*//*


}
*/
