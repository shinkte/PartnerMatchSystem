package com.shinkte.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: shinkte
 * @Description: 加入队伍请求传输对象
 * @CreateTime: 2024-11-23
 */
@Data
public class TeamJionRequest implements Serializable {
    private static  final long serialVersionUID = -3770853280971662538L;

    /**
     * 队伍名称
     */
    private String TeanmId;

    private String passWord;


}
