package com.clint.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clint.reggie.dto.SetmealDto;
import com.clint.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {

    void saveWithSetmealDish(SetmealDto setmealDto);

    SetmealDto getByIdWithSetmealDish(Long id);

    void update(SetmealDto setmealDto);

    void removeWithSetmealDish(Long[] ids);
}
