package com.shinkte.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shinkte.common.ErrorCode;
import com.shinkte.enums.TeamStatusEnum;
import com.shinkte.mapper.UserTeamMapper;
import com.shinkte.model.domain.Team;
import com.shinkte.model.domain.User;
import com.shinkte.exception.BusinessException;
import com.shinkte.model.domain.UserTeam;
import com.shinkte.service.TeamService;
import com.shinkte.mapper.TeamMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
* @author shinkte
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-11-18 10:31:58
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {
    @Resource
    private UserTeamServiceImpl userTeamService;  //建议注入到service层，可能还需要执行其他业务逻辑判断
    @Override
    @Transactional(rollbackFor = Exception.class)   //事务注解,表示该方法是一个事务方法则方法内的所有数据库操作要么全部成功并提交，要么在遇到异常时全部回滚
    public long addTeam(Team team, User loginUser) {
        //1.请求参数是否为空？
        if (team == null || loginUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.是否登录，未登录不允许创建
        if (loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //3.校验信息
        //4.队伍人数 > 1 且 <= 20
         int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
         if (maxNum < 1 || maxNum > 20){
             throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
         }
        //5.队伍标题 <= 20
        String tile = team.getName();
         if(StringUtils.isAnyBlank()|| tile.length() > 20){
             throw  new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不满足要求");
         }
        //6.描述 <= 512
        String desc=team.getDescription();
         if (StringUtils.isAnyBlank(desc) && desc.length() > 512){
             throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述不满足要求");
         }
        //7.status 是否公开（int）不传默认为 0（公开）
        int status =Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnum(status);
        if (teamStatusEnum == null){
             throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
         }
        //8.如果 status 是加密状态，一定要有密码，且密码 <= 32
        String passwd=team.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if (StringUtils.isAnyBlank(passwd)||passwd.length()>32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
            }
        }
        //9.超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"已过时");
        }
        //10.校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        final  long id = loginUser.getId();
        queryWrapper.eq("userId",id);
        long hasCreateTeamCount = this.count(queryWrapper);
        if (hasCreateTeamCount >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建 5 个队伍");
        }
        /**
         *第11/12步 需要实现同时插入或者都不能插入------>事务（原子性）---->引入transactional注解
         */
        //11.插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(loginUser.getId());
        boolean result = this.save(team);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        //12.插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍关系失败");
        }
        return team.getId();
    }
}



