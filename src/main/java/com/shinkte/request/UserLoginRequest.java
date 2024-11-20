package com.shinkte.request;



import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author shinkte
 * @from shinkte
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    // https://www.code-nav.cn/
}
