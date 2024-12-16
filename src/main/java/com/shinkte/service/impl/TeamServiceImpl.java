package com.shinkte.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shinkte.common.ErrorCode;
import com.shinkte.enums.TeamStatusEnum;
import com.shinkte.model.domain.Team;
import com.shinkte.model.domain.User;
import com.shinkte.exception.BusinessException;
import com.shinkte.model.domain.UserTeam;
import com.shinkte.model.dto.TeamQuery;
import com.shinkte.model.request.TeamJionRequest;
import com.shinkte.model.request.TeamQuitRequest;
import com.shinkte.model.request.TeamUpdateRequest;
import com.shinkte.model.vo.TeamUserVo;
import com.shinkte.model.vo.UserVo;
import com.shinkte.service.TeamService;
import com.shinkte.mapper.TeamMapper;
import com.shinkte.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    @Resource
    private UserServiceImpl userServiceImpl;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */

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

    /**
     * 查询队伍列表
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    @Override
    public List<TeamUserVo> listTeamUser(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery !=null){
            Long id1 = teamQuery.getId();
            if (id1!=null &&id1>0){
                queryWrapper.eq("id",id1);
            }
            List<Long> idList = teamQuery.getIdList();
            if (!CollectionUtils.isEmpty(idList)){
                queryWrapper.in("id",idList);
            }
            String serachText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(serachText)){
                queryWrapper.and(qw->qw.like("name",serachText).or().like("description",serachText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            String description1 = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description1)){
                queryWrapper.like("description",description1);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum!=null && maxNum>0){
                queryWrapper.eq("maxNum",maxNum);
            }
            Integer status = teamQuery.getStatus();
            //只有管理员才能查看机密队伍信息
            TeamStatusEnum anEnum = TeamStatusEnum.getEnum(status);
            if(anEnum ==null){
                anEnum=TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin && anEnum.equals(TeamStatusEnum.SECRET)){
                throw new BusinessException(ErrorCode.NOT_PERMISSION,"没有权限查看该队伍信息");
            }
            queryWrapper.eq("status",anEnum.getValue());
            Date expireTime = teamQuery.getExpireTime();
            if (expireTime!=null){
                queryWrapper.le("expireTime",expireTime);
            }
        }
        //不展示已过期的队伍
        queryWrapper.and(queryWrapper1->queryWrapper1.gt("expireTime", new Date())
                .or().isNull("expireTime")); //queryWrapper1的多个条件查询
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        //关联查询用户信信息
        //1.使用sql实现关联查询
                //实现查找队伍和创建人的信息
                 /**
                    * select * from team t left join user u on t.user_id=u.id;
                 */
                //实现查询队伍和已加入队伍的成员信息
                 /**
                * select * from team t left join user_team ut on t.id=ut.teamId
                     *     left join  user on ut.useriD=user.id;
                 */
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
       //2.关联查询创建人的用户信息
        for (Team team : teamList){
            Long userId = team.getUserId();
            if (userId==null){
                continue;
            }
            User createUser = userServiceImpl.getById(userId);
            //用户信息脱敏
            User safetyUser = userServiceImpl.getSafetyUser(createUser);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team,teamUserVo);
            if(createUser!=null){
                UserVo   userVo = new UserVo();
                BeanUtils.copyProperties(safetyUser,userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;
    }

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser) {
        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if(id==null || id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍id不能为空");
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        //只有管理员或者队长可以修改队伍信息
        if(!userServiceImpl.isAdmin(loginUser)&& oldTeam.getUserId()!=loginUser.getId()){
            throw new BusinessException(ErrorCode.NOT_PERMISSION,"没有权限修改该队伍信息");
        }
        //队伍若加密状态则必须设置密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnum(teamUpdateRequest.getStatus());
        if (statusEnum==TeamStatusEnum.SECRET) {
            if (StringUtils.isNotBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密状态必须设置密码");
            }
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,team);
        boolean result = this.updateById(team);
        return result;
    }

    @Override
    public boolean joinTeam(TeamJionRequest teamJionRequest,User loginUser) {
        if (teamJionRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        }
            //        1、用户只能加入五个队伍
        long id = loginUser.getId();
        //todo 思考为什么加锁? 以及使用synchronized可能出现的问题  --->这是单机锁
        //todo 最后为什么使用分布式锁？

            //        2、队伍必须存在 且只能加入未满、未过期的队伍
        Long teanmId = teamJionRequest.getTeamId();
        if (teanmId ==null || teanmId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍id不能为空");
           }
        Team team = this.getById(teanmId);
        Integer maxNum = team.getMaxNum();
        Long teamId = team.getId();
        if (team == null ){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        if(team.getExpireTime().before(new Date())||team.getExpireTime()==null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }
        //5、若加入的队伍是加密的 必须密码匹配
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnum(status);
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (!teamJionRequest.getPassWord().equals(team.getPassword()) || StringUtils.isBlank(teamJionRequest.getPassWord())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        //        6、禁止加入私有的队伍
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.NOT_PERMISSION, "不能加入私有队伍");
        }
            RLock lock = redissonClient.getLock("shinkte:joinTeam:lock");
            try{
                //抢到锁并执行
                while(true){
                    if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                        System.out.println("getLock: " + Thread.currentThread().getId());
                        //只能加入未满的队伍

                        long userHasJoinTeamCount = this.getTeamUserCount(teamId);
                        if (userHasJoinTeamCount >=team.getMaxNum()){
                            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已满");
                        }
                        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("userId",id);
                        long count = userTeamService.count(queryWrapper);
                        if (count >= 5){
                            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户只能加入五个队伍");
                        }
                        //        3、不能重复加入已加入的队伍（幂等性）
                        QueryWrapper<UserTeam> userHasJoinTeamMapper=new QueryWrapper<>();
                        userHasJoinTeamMapper.eq("userId",id);
                        userHasJoinTeamMapper.eq("teamId",teamId);
                        long count1 = userTeamService.count(userHasJoinTeamMapper);
                        if (count1>0){
                            throw new BusinessException(ErrorCode.PARAMS_ERROR,"已加入该队伍");
                        }

                        //        7、新增队伍-用户关联表信息
                        UserTeam userTeam = new UserTeam();
                        userTeam.setUserId(id);
                        userTeam.setTeamId(teamId);
                        userTeam.setJoinTime(new Date());
                        return userTeamService.save(userTeam);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }finally {
                //只能释放自己的锁
                if(lock.isHeldByCurrentThread()){
                    System.out.println("releaseLock: " + Thread.currentThread().getId());
                    lock.unlock();
                }
            }
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        }
        Long teanmId = teamQuitRequest.getTeamId();

        Team team = this.getTeam(teanmId);
        //        4、校验当前用户是否已加入队伍
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long count = userTeamService.count(queryWrapper);
        if (count <=0){
            throw new BusinessException(ErrorCode.NO_AUTH,"未加入该队伍");
        }

        //        1、队长退出队伍
        //          a、队伍中只有队长一人则解散队伍
        Long teamUserCount = this.getTeamUserCount(team.getId());
        if (teamUserCount==1){
            //删除队伍信息
            this.removeById(team.getId());
            //删除队伍-用户关联表信息
            QueryWrapper<UserTeam> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("teamId",team.getId());
           return  userTeamService.remove(queryWrapper1);

        }else {
            //          b、若不止队长以上则移交队长权限只第二个加入队伍的用户
            if(team.getUserId()==userId){
                //查询队伍中所有用户加入队伍时间
                QueryWrapper<UserTeam> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("teamId",team.getId());
                queryWrapper2.last("order by id asc limit 2");  //使用queeWrapper的last方法，可以在sql语句之后拼接其他语句
                List<UserTeam> userTeamList = userTeamService.list(queryWrapper2);
                if (CollectionUtils.isEmpty(userTeamList)||userTeamList.size()<2){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍信息异常");
                }
                //将第二个加入队伍的用户设置为队长
                UserTeam userTeam = userTeamList.get(1);
                Long nextTeamLeaderId = userTeam.getUserId();
                //更新队伍信息
                Team team1 = new Team();
                team1.setId(team.getId());
                team1.setUserId(nextTeamLeaderId);
                boolean result= this.updateById(team1);
                if (!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队长失败");
                }
                //删除队伍-用户关联表信息
                QueryWrapper<UserTeam> userTeamMapper =new QueryWrapper<>();
                userTeamMapper.eq("userId",userId);
                userTeamMapper.eq("teamId",team.getId());
                return userTeamService.remove(userTeamMapper);
            }
        }
        return false;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)   //事务注解,表示该方法是一个事务方法则方法内的所有数据库操作要么全部成功并提交，要么在遇到异常时全部回滚
    public boolean deleteTeam(long teamId, User loginUser) {
        //      2、检测队伍是否存在
        Team team = this.getTeam(teamId);
        //      3、校验当前是否为队长
        if(team.getUserId()!=loginUser.getId()){
            throw new BusinessException(ErrorCode.NOT_PERMISSION,"非队长不能删除队伍");
        }
        //      4、移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamMapper =new QueryWrapper<>();
        userTeamMapper.eq("teamId",team.getId());
        boolean remove = userTeamService.remove(userTeamMapper);
        if (!remove){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍-用户关联表信息失败");
        }
        //      5、删除队伍
        return this.removeById(team.getId());
    }

    /**
     * 获取队伍中的用户数量
     * @param teamId
     * @return
     */
    private Long getTeamUserCount(long teamId){
    QueryWrapper<UserTeam> queryWrapper1 = new QueryWrapper<>();
    queryWrapper1.eq("teamId",teamId);
    long userHasJoinTeamCount = userTeamService.count(queryWrapper1);
    return userHasJoinTeamCount;
    }

    /**
     * 获取队伍信息
     * @param teamId
     * @return
     */
    private Team getTeam(Long teamId) {
        if (teamId ==null || teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍id不能为空");
        }
        //        3、检验队伍是否存在
        Team team = this.getById(teamId);
        if (team==null ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        return team;
    }
}



