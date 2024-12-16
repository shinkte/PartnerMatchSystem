package com.shinkte.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shinkte.Utils.AlogrithmUtils;
import com.shinkte.common.BaseResponse;
import com.shinkte.common.ErrorCode;
import com.shinkte.exception.BusinessException;
import com.shinkte.model.domain.User;
import com.shinkte.model.vo.UserVo;
import com.shinkte.service.UserService;
import com.shinkte.mapper.UserMapper;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.shinkte.contant.UserConstant.ADMIN_ROLE;
import static com.shinkte.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author shinkte
 * @from shinkte
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    // https://www.code-nav.cn/

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "shinkte";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签查找用户，使用内存查询
     *
     * @param tagNameList
     * @return 用户列表
     */
    @Override
    public List<User> searchUsersByTagsByMemory(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签为空");
        }
        //使用内存查询
        //1、先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(queryWrapper);

        Gson gson = new Gson();
        //2.在内存中查询是否有包含的标签
        return users.stream().filter(user -> {
            String tagstr=user.getTags(); //注意标签字段是json格式
            if(StringUtils.isEmpty(tagstr)){
                return false;
            }
            List<String> tagNameLi= gson.fromJson(tagstr, new TypeToken<List<String>>() {}.getType());
            Set<String> tagNameSet = new HashSet<>(tagNameLi);
            tagNameSet=Optional.ofNullable(tagNameSet).orElse(new HashSet<>());
            for(String tagName:tagNameSet){
                if(!tagNameList.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据标签查找用户，使用sql查询
     * @param tagNameList
     * @return  用户列表
     */
    @Deprecated      //不推荐使用，sql查询效率低
    public List<User> searchUsersByTagsBySql(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接tag，使用sq查询
        for (String tagName : tagNameList) {
            //like（）查询自动添加%进行查询
            queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
      /*  stream()：将集合转换为stream流。流是一种支持顺序和并行聚合操作的元素序列（并行聚合操作：指的是对多个数据源并行处理，然后将结果汇总）
        map（）：对流中每一个元素应用一个函数，并返回一个新的流
        collect（）：将流中的元素收集到一个集合中
        Collectors.toList()：将流中的元素收集到一个List集合中*/
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 更新用户信息
     *主要是校验
     * @param user
     * @param loginUser
     * @return 1成功，0失败
     */
    @Override
    public int UpdateUser(User user, User loginUser) {
        long userid = user.getId();
        //Todo 校验,若是用户传入参数为空，则可能暴露数据库信息
        //若是管理员，则可以修改任何用户信息
        if(!isAdmin(loginUser)){
            if(userid <=0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户id为空");
            }
            User oldUser = userMapper.selectById(userid);
            if(oldUser == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
            }
            return userMapper.updateById(user);
        }
        //若是普通用户，则只能修改自己的信息
        if(userid!= loginUser.getId()){
            throw new BusinessException(ErrorCode.NOT_PERMISSION,"无权限修改");
        }
        User oldUser = userMapper.selectById(userid);
        if(oldUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        return userMapper.updateById(user);
    }
    /**
     * 获取登录用户信息
     *
     * @param request
     * @return 登录用户信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request==null){
            return null;
        }
        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(user== null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) user;
    }

    /**
     * 判断当前用户是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }


    /**
     * 判断当前用户是否为管理员
     * 方法重载
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {

        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

/*优化前方式，比较耗时
@Override
    public List<User> matchUser(long num, User loginUser) {
        List<User> UserList = this.list();
        String loginUsertags = loginUser.getTags();
        //将tags转换为标签数组
        Gson gson = new Gson();
        List<String> loginUserTagList = gson.fromJson(loginUsertags, new TypeToken<List<String>>() {
        }.getType());
        //循环遍历数据库匹配
        SortedMap<Integer,Long> indexDistanceMap =new TreeMap<>();
        for (int i=0;i<UserList.size();i++){
            User user=new User();
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags)){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            //计算分数
            long distance= AlogrithmUtils.minimumEditDistance(loginUserTagList,userTagList);
            indexDistanceMap.put(i,distance);
        }
        List<Integer> maxDistanceIndexList = indexDistanceMap.keySet().stream().limit(num).collect(Collectors.toList());
        List<User> userVoList=maxDistanceIndexList.stream()
                .map(index->getSafetyUser(UserList.get(index)))
                .collect(Collectors.toList());
        return userVoList;
    }*/

    /**
     * 优化后的方式数据库检索
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUser(long num, User loginUser) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //优化点一：去除空标签的用户查询,数据库会将[]判断为非空，导致查询结果为全部
        queryWrapper.isNotNull("tags").ne("tags","[]");
        //优化点二：只查询需要的数据，ID和tags===>性能提升50%
        queryWrapper.select("id","tags");
        List<User> UserList = this.list(queryWrapper);
        String loginUsertags = loginUser.getTags();
        //将tags转换为标签数组
        Gson gson = new Gson();
        List<String> loginUserTagList = gson.fromJson(loginUsertags, new TypeToken<List<String>>() {}.getType());

    //循环遍历数据库匹配
        //维护一个定长的数组，存放用户和分数
        //Pair<User,Long>,User是用户对象，Long是分数，Pair通常用来存储键值对或者两个相关联的对象，思考为什么不适用map
        List<Pair<User,Long>> list=new ArrayList<>();
        //依次计算当前用户和所有用户的相似度
        for (int i=0;i<UserList.size();i++){
            User user=UserList.get(i);
            String userTags = user.getTags();
            //去除自己的条件
            if (userTags==null ||userTags.isEmpty() || user.getId()==loginUser.getId()){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {}.getType());
         //计算分数
            long distance= AlogrithmUtils.minimumEditDistance(loginUserTagList,userTagList);
            list.add(new Pair<>(user,distance));
     }
        /**
         * 将List列表转换一个流，后对流进行排序（按分数从小到大排序），然后取前num个元素，最后将结果转换为List列表返回
         * sorted(Comparator<T> comparator)：对流进行排序，Comparator<T>是一个比较器接口，用于比较两个元素的大小，使用方法为：
         * (a,b)->(int)(a.getValue()-b.getValue())，a.getValue()和b.getValue()是两个元素的分数，a和b是流的两个元素。
         */
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());

        /**
         * 将一个包含用户和距离的列表转换为流，然后将使用map处理其中每个Pair对象，并提取其中的用户ID，最后转换为List列表返回
         * 其中map用于对流中的每个元素进行转换或映射
         */
        List<Long> userIdList=topUserPairList.stream()
                .map(Pair->Pair.getKey().getId())
                .collect(Collectors.toList());

        //返回用户数据全部信息
        QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
        //会影响原有的排序，需要额外处理
        queryWrapper1.in("id",userIdList);
        /**
         * 调用数据库查询，获取满足条件的User对象列表，然后将其返回的数据列表转换为流，后对每个元素调用getSafetyUser方法，最后将处理后的User对象按照Id分组，返回一个新的Map对象
         * collect是对流中的数据进行聚合操作，Collectors.groupingBy(Function<? super T, ? extends K> keyExtractor)：用于将流中的元素按照keyExtractor指定的键进行分组，返回一个Map对象，其中key是分组的键，value是分组后的元素列表
         */
        Map<Long, List<User>> userIdUserListMap = this.list(queryWrapper1)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> matchUserLists=new ArrayList<>();
        for (Long userId:userIdList){
            matchUserLists.add(userIdUserListMap.get(userId).get(0));
        }
        return matchUserLists;
    }
}