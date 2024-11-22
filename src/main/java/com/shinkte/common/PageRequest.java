package com.shinkte.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: shinkte
 * @Description: 页面请求类
 * @CreateTime: 2024-11-19
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 2349055607828088106L;
    /**
     * 页码
     */
    private int pageNum=1;
    /**
     * 每页显示数量
     */
    private int pageSize=10;

}
