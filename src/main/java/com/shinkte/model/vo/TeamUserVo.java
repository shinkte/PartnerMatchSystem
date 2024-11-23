package com.shinkte.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: shinkte
 * @Description: 返回值类--Team表封装类
 * @CreateTime: 2024-11-23
 */
@Data
public class TeamUserVo implements Serializable {

    private static final long serialVersionUID = 3724998068656945750L;
    /**
     * id
     */
    private Long id;

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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;
    /**
     * 创建人信息
     */
    private UserVo  createUser;

}
