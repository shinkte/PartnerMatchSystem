package com.shinkte.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
/**
 * @Author: shinkte
 * @Description: RedissonCRUD
 * @CreateTime: 2024-11-15
 */
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;
    @Test
    public void testRedisson() {
        //list  :数据存储在本地JVM内存中
        List<String> list=new ArrayList<>();
        list.add("shinkte");
        list.get(0);
        //list.remove(0);

        //数据存储在redis中
        RList<String> redissonClientList = redissonClient.getList("redisson-list");
        //redissonClientList.add("shinkte");
        System.out.println(redissonClientList.get(0));
        redissonClientList.remove(0);


        //map
        Map<String, String> map = new HashMap<>();
        map.put("name", "shinkte");
        map.get("name");
        //map.remove("name");

        //数据存储在redis中
        Map<String, String> redissonClientMap = redissonClient.getMap("redisson-map");
        //redissonClientMap.put("name", "shinkte");
        System.out.println(redissonClientMap.get("name"));
        redissonClientMap.remove("name");


        //set
        Set<String> set = new HashSet<>();
        set.add("shinkte");
        set.contains("shinkte");
        //set.remove("shinkte");
        //数据存储在redis中
        Set<String> redissonClientSet = redissonClient.getSet("redisson-set");
        //redissonClientSet.add("shinkte");
        System.out.println(redissonClientSet.contains("shinkte"));
        redissonClientSet.remove("shinkte");

        //stack
    }
}
