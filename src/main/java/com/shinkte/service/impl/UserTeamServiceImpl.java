package com.shinkte.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shinkte.model.domain.UserTeam;
import com.shinkte.service.UserTeamService;
import com.shinkte.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author shinkte
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-11-18 10:31:22
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




