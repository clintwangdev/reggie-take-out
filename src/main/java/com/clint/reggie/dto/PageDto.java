package com.clint.reggie.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工前端接口类
 */
@Data
public class PageDto {

    /**
     * 页数
     */
    private Integer page;

    /**
     * 每页显示条数
     */
    private Integer pageSize;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 订单号
     */
    private Long number;

    /**
     * 订单开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 订单结束时间
     */
    private LocalDateTime endTime;

}
