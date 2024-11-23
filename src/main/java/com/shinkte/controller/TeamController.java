package com.shinkte.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shinkte.common.BaseResponse;
import com.shinkte.common.ErrorCode;
import com.shinkte.common.ResultUtils;
import com.shinkte.model.domain.User;
import com.shinkte.model.dto.TeamQuery;
import com.shinkte.model.domain.Team;
import com.shinkte.exception.BusinessException;
import com.shinkte.model.request.TeamAddRequest;
import com.shinkte.model.request.TeamJionRequest;
import com.shinkte.model.request.TeamUpdateRequest;
import com.shinkte.model.vo.TeamUserVo;
import com.shinkte.service.TeamService;
import com.shinkte.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    public BaseResponse<Boolean>  deleteTeam(@RequestBody long id ){
        if (id ==0){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息不能为空");
        }
        boolean result = teamService.removeById(id);
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
}
