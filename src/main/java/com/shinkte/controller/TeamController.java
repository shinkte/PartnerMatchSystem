package com.shinkte.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shinkte.common.BaseResponse;
import com.shinkte.common.ErrorCode;
import com.shinkte.common.ResultUtils;
import com.shinkte.contant.TeamQuery;
import com.shinkte.domain.Team;
import com.shinkte.exception.BusinessException;
import com.shinkte.service.TeamService;
import com.shinkte.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    public BaseResponse<Long>  addTeam(@RequestBody Team team){
        if (team ==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息不能为空");
        }
        boolean save = teamService.save(team);
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存团队信息失败");
        }
        return ResultUtils.success(save?team.getId():0L);
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
    public BaseResponse<Long>  updateTeam(@RequestBody Team team){
        if (team ==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
        }
        boolean result = teamService.updateById(team);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新团队信息失败");
        }
        return ResultUtils.success(result?team.getId():0L);
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
    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeam(TeamQuery teamQuery){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"团队信息参数不能为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(team,teamQuery);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        List<Team> teamList= teamService.list(queryWrapper);
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
}
