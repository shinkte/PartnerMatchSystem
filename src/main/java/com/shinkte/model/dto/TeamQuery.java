package com.shinkte.model.dto;

import com.shinkte.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;


/**
 * @Author: shinkte
 * @Description: 队伍查询封装类
 * @CreateTime: 2024-11-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery  extends PageRequest {
    /**
     * id
     */
    private Long id;
    /**
     * id列表，用于批量查询
     */
    private List<Long> idList;

    /**
     * 队伍名称
     */
    private String name;
    /**
     * 搜索关键词（同时对队伍名称和描述进行查询）
     */
    private String searchText;

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
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     *
     */

}
