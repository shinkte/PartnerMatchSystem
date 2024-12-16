package com.shinkte.service;

import com.shinkte.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shinkte.model.domain.User;
import com.shinkte.model.dto.TeamQuery;
import com.shinkte.model.request.TeamJionRequest;
import com.shinkte.model.request.TeamQuitRequest;
import com.shinkte.model.request.TeamUpdateRequest;
import com.shinkte.model.vo.TeamUserVo;

import java.util.List;

/**
* @author shinkte
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-11-18 10:31:58
*/
public interface TeamService extends IService<Team> {

    /**
     * 新增队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 查询队伍列表
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeamUser(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJionRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJionRequest teamJionRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 队长解散队伍
     * @param teamId
     *
     */
    boolean deleteTeam(long teamId, User loginUser);
}
