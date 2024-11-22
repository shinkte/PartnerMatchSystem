package com.shinkte.FirstTasks;

import com.shinkte.mapper.UserMapper;
import com.shinkte.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;
import com.shinkte.model.domain.User;
import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
@RunWith(SpringRunner.class)
public class InsertUserDataTest {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    /**
     * 数据直接插入数据库
     */
/*    @Test
    public void insertUserData() {
        StopWatch stopWatch = new StopWatch(); // 计时器
        stopWatch.start();
        final int INSERT_NUM=   100000;
        for(int i=0;i<INSERT_NUM;i++){
            User user=new User();
            user.setUsername("假用户");
            user.setUserAccount("Fakeshinte");
            user.setAvatarUrl("https://img.yzcdn.cn/vant/user-inactive.png");
            user.setGender("男");
            user.setUserPassword("12345678");
            user.setPhone("3r892374t834t8345");
            user.setEmail("");
            user.setUserStatus(0);
            user.setIsDelete(0);
            user.setUserRole(0);
            user.setPlanetCode("4432");
            user.setTags("[]");
            user.setUserProfile("");
            userMapper.insert(user);

        }
        stopWatch.stop();
        System.out.println("插入"+INSERT_NUM+"条数据，耗时："+stopWatch.getTotalTimeMillis()+"毫秒");
    }*/

    /**
     * 并发批量插入数据库
     */
    @Test
    public void doConcurrentcyInsertUser(){
        StopWatch stopWatch = new StopWatch(); // 计时器
        stopWatch.start();
        final int INSERT_NUM=   10000;
        int j=0; //分10组
        List<CompletableFuture<Void>> futureList=new ArrayList<>();

        for(int i=0;i<10;i++){
            List<User> userList=new ArrayList<>();
            while(true){
                j++;
                User user=new User();
                user.setUsername("假用户");
                user.setUserAccount("Fakeshinte");
                user.setAvatarUrl("https://img.yzcdn.cn/vant/user-inactive.png");
                user.setGender("男");
                user.setUserPassword("12345678");
                user.setPhone("3r892374t834t8345");
                user.setEmail("");
                user.setUserStatus(0);
                user.setIsDelete(0);
                user.setUserRole(0);
                user.setPlanetCode("4432");
                user.setTags("[]");
                user.setUserProfile("");
                userList.add(user);
                if(j%10000==0){
                    break;
                }
            }
            CompletableFuture<Void> future=CompletableFuture.runAsync(()->{
                System.out.println("TheradNmae:"+Thread.currentThread().getName()+"开始执行");
                userService.saveBatch(userList,1000);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println("批量插入"+INSERT_NUM+"条数据，耗时："+stopWatch.getTotalTimeMillis()+"毫秒");
    }
}