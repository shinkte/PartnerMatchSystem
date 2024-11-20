package com.shinkte.FirstTasks;

import com.shinkte.mapper.UserMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;




@Component
public class InsertUserData {
    @Resource
    private UserMapper userMapper;

/*    @Scheduled(fixedDelay = 5000,fixedRate = Long.MAX_VALUE)
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
}
