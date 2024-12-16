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
     * 是否已加入
     */
   // private boolean isJoin=false;  不建议is开头的变量名，因为字段的序列化行为可能受到Java Bean规范的限制。
    // Java 的序列化工具（如 Jackson）会自动根据字段名生成 JSON 属性名。如果字段是 boolean 类型，并且以 is 开头，则默认会去掉 is，并将后面的部分首字母小写作为 JSON 属性名。
    private boolean hasJoin=false;
    /**
     * 创建人信息
     */
    private UserVo  createUser;
    /**
     * 加入人员树
     */
    private Integer hahJoinNumber=0;

}
