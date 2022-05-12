package com.clint.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clint.reggie.dto.DishDto;
import com.clint.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);

    void removeWithFlavor(Long[] ids);

}
