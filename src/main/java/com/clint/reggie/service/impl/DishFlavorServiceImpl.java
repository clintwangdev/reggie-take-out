package com.clint.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clint.reggie.entity.DishFlavor;
import com.clint.reggie.mapper.DishFlavorMapper;
import com.clint.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

/**
 * 菜品口味业务层实现类
 */
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {

}
