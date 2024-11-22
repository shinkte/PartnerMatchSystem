package com.shinkte.service;

import com.shinkte.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shinkte.model.domain.User;

/**
* @author shinkte
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-11-18 10:31:58
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);

}
