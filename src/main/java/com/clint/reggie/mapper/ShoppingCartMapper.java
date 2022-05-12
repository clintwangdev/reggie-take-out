package com.clint.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clint.reggie.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
