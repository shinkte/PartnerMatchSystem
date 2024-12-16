package com.shinkte.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shinkte.common.BaseResponse;
import com.shinkte.common.ErrorCode;
import com.shinkte.common.ResultUtils;
import com.shinkte.exception.BusinessException;
import com.shinkte.model.domain.User;
import com.shinkte.model.request.UserLoginRequest;
import com.shinkte.model.request.UserRegisterRequest;
import com.shinkte.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.shinkte.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author shinkte
 * @from shinkte
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173/"},allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request :该对象主要获取与当前HTTO请求相关的信息，包括请求头、请求参数、会话信息等。
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request ) {
        //从HTTP请求的会话中获取存储的用户状态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    // /

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return  用户列表
     */

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags( @RequestParam(required = false) List<String> tagNameList) {
        if (tagNameList == null || tagNameList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTagsBySql(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 更新用户信息
     * @param user
     * @param request
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        //校验参数是否为空
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验权限(service层校验)
        User loginUser = userService.getLoginUser(request);

        int integer = userService.UpdateUser(user, loginUser);
        //信息更新
        return ResultUtils.success(integer);
    }
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers( int pageSize,  int pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        //先判断缓存是否存储推荐列表
        String redisKey=String.format("shinkte:user:recommend:%s",loginUser.getId());
        Page<User> userPage =(Page<User>) redisTemplate.opsForValue().get(redisKey);
        if(userPage!=null){
            return ResultUtils.success(userPage);
        }
        //缓存中没有推荐列表，则从数据库中查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);  //使用分页查询需要导入拦截器
        //List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        //将查询结果缓存到redis中
        try {
            redisTemplate.opsForValue().set(redisKey,userPage,10000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResultUtils.success(userPage);
    }
    /**
     * 匹配标签相似度最高的用户
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUser(long num, HttpServletRequest request){
        if (num<=0||num>10){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"num参数必须在1-10之间");
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUser(num,loginUser));
    }
}
