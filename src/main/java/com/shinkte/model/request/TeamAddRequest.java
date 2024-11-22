package com.shinkte.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: shinkte
 * @Description: 队伍表传输对象
 * @CreateTime: 2024-11-23
 */
@Data
public class TeamAddRequest implements Serializable {
    private static  final long serialVersionUID = -3770853280971662538L;

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



}
