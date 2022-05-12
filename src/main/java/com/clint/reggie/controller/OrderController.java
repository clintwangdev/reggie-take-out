package com.clint.reggie.controller;

import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGMacAddrExpr;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clint.reggie.common.BaseContext;
import com.clint.reggie.common.R;
import com.clint.reggie.dto.PageDto;
import com.clint.reggie.entity.Orders;
import com.clint.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        orderService.submit(orders);
        return R.success("提交订单成功");
    }

    /**
     * 分页获取所有订单列表
     */
    @GetMapping("/page")
    public R<Page<Orders>> page(PageDto pageDto) {
        Page<Orders> ordersPage = new Page<>(pageDto.getPage(), pageDto.getPageSize());

        // 设置过滤条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 订单号
        queryWrapper.eq(pageDto.getNumber() != null, Orders::getNumber, pageDto.getNumber());
        // 开始时间
        queryWrapper.gt(pageDto.getBeginTime() != null, Orders::getOrderTime, pageDto.getBeginTime());
        // 结束时间
        queryWrapper.lt(pageDto.getEndTime() != null, Orders::getOrderTime, pageDto.getEndTime());

        orderService.page(ordersPage, queryWrapper);

        return R.success(ordersPage);
    }

    /**
     * 获取登录用户的订单列表
     */
    @GetMapping("/userPage")
    public R<Page<Orders>> userPage(PageDto pageDto) {
        Page<Orders> ordersPage = new Page<>(pageDto.getPage(), pageDto.getPageSize());

        // 获取登录用户 ID
        long userId = BaseContext.getCurrentId();

        // 设定过滤条件，仅查询当前登录用户
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, userId);

        orderService.page(ordersPage, queryWrapper);

        return R.success(ordersPage);
    }

    /**
     * 修改订单状态
     */
    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders) {

        orderService.updateById(orders);

        return R.success("修改订单状态成功");
    }
}
