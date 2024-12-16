package com.shinkte.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shinkte.common.BaseResponse;
import com.shinkte.common.ErrorCode;
import com.shinkte.common.ResultUtils;
import com.shinkte.model.domain.User;
import com.shinkte.model.domain.UserTeam;
import com.shinkte.model.dto.TeamQuery;
import com.shinkte.model.domain.Team;
import com.shinkte.exception.BusinessException;
import com.shinkte.model.request.*;
import com.shinkte.model.vo.TeamUserVo;
import com.shinkte.service.TeamService;
import com.shinkte.service.UserService;
import com.shinkte.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组队接口
 *
 * @author shinkte
 * @from shinkte
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173/"},allowCredentials = "true")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long>  addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest  httpServletRequest){
        if (teamAddRequest ==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息不能为空");
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long l = teamService.addTeam(team, loginUser);
        return ResultUtils.success(l);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean>  deleteTeam(@RequestBody TeamDeleteRequest teamDeleteRequest, HttpServletRequest  httpServletRequest){
        long teamId = teamDeleteRequest.getTeamId();
        if (teamId ==0){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息不能为空");
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamService.deleteTeam(teamId,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除团队信息失败");
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean>  updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest  httpServletRequest){
        if (teamUpdateRequest ==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
        }
        User loginUser=userService.getLoginUser(httpServletRequest);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新团队信息失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id){
        if (id<=0){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
        }
        Team team = teamService.getById(id);
        if (team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息不存在");
        }
        return ResultUtils.success(team);
    }
/*    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeam(TeamQuery teamQuery){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(team,teamQuery);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        List<Team> teamList= teamService.list(queryWrapper);
        return ResultUtils.success(teamList);
    }*/
@GetMapping("/list")
public BaseResponse<List<TeamUserVo>> listTeam(TeamQuery teamQuery,HttpServletRequest  httpServletRequest){
    if (teamQuery==null){
        throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
    }
     boolean isAdmin= userService.isAdmin(httpServletRequest);
    List<TeamUserVo> teamList= teamService.listTeamUser(teamQuery,isAdmin);
    //返回当前用户加入了那些队伍，因为前端界面需要这个参数作为判断条件
    final List<Long> teamIdList=teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());
    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
    try{
        User loginUser=userService.getLoginUser(httpServletRequest);
        userTeamQueryWrapper.eq("userId",loginUser.getId());
        userTeamQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> UserTeamlist = userTeamService.list(userTeamQueryWrapper);
        Set<Long> hasJoinTeamIdList = UserTeamlist.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        teamList.forEach(team->{
            boolean hasJoin=hasJoinTeamIdList.contains(team.getId());
            team.setHasJoin(hasJoin);
        });

    }catch (Exception e){
        e.printStackTrace();
    }
    //查询加入队伍的用户信息（人数）
    QueryWrapper<UserTeam> userJoinTeamCountQueryWrapper = new QueryWrapper<>();
    userJoinTeamCountQueryWrapper.in("teamId",teamIdList);
    List<UserTeam> list = userTeamService.list(userJoinTeamCountQueryWrapper);
    Map<Long, List<UserTeam>> teamIdUserTeamList = list.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
    teamList.forEach(team->{
        team.setHahJoinNumber(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size());
    });
    return ResultUtils.success(teamList);
}

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamPage(TeamQuery teamQuery){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page=new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage= teamService.page(page,queryWrapper);
        return ResultUtils.success(teamPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJionRequest teamJionRequest,HttpServletRequest  httpServletRequest){
    if (teamJionRequest==null){
        throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
    }
    User loginUser=userService.getLoginUser(httpServletRequest);
    boolean result=teamService.joinTeam(teamJionRequest,loginUser);
    return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest  httpServletRequest){
    if(teamQuitRequest==null){
        throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
    }
    User loginUser=userService.getLoginUser(httpServletRequest);
    boolean result=teamService.quitTeam(teamQuitRequest,loginUser);
    return ResultUtils.success(result);
    }



    /**
     * 获取当前用户创建的队伍
     * @param teamQuery
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/list/currentUser")
    public BaseResponse<List<TeamUserVo>> listMyCreatedTeam(TeamQuery teamQuery,HttpServletRequest  httpServletRequest){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
        }
        User loginUser =userService.getLoginUser(httpServletRequest);
        teamQuery.setUserId(loginUser.getId());
        boolean admin = userService.isAdmin(httpServletRequest);
        List<TeamUserVo> teamList= teamService.listTeamUser(teamQuery,true);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取当前用户加入的队伍
     * @param teamQuery
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/list/Joined")
    public BaseResponse<List<TeamUserVo>> listMyJoinedTeam(TeamQuery teamQuery,HttpServletRequest  httpServletRequest){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
        }
        //获取当前用户信息
        User loginUser =userService.getLoginUser(httpServletRequest);
        //获取当前用户加入的队伍Id表
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //取出不重复的队伍id
        Map<Long, List<UserTeam>> collect = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> teamIdList = new ArrayList<>(collect.keySet());
        teamQuery.setIdList(teamIdList);
        List<TeamUserVo> teamList= teamService.listTeamUser(teamQuery,true);
        return ResultUtils.success(teamList);
    }
}
