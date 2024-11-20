package com.shinkte.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shinkte.domain.Team;
import com.shinkte.service.TeamService;
import com.shinkte.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author shinkte
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-11-18 10:31:58
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




