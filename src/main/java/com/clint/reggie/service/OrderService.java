package com.clint.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clint.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    void submit(Orders orders);
}
