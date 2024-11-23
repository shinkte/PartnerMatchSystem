package com.shinkte.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: shinkte
 * @Description: 队伍表更新包装类
 * @CreateTime: 2024-11-24
 */
@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = -2392565237015631868L;
    private long id;
    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 密码
     */
    private String password;

    /**
     * 过期时间
     */
    private Date expireTime;
    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

}
