package com.clint.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clint.reggie.dto.SetmealDto;
import com.clint.reggie.entity.Setmeal;
import com.clint.reggie.entity.SetmealDish;
import com.clint.reggie.mapper.SetmealMapper;
import com.clint.reggie.service.SetmealDishService;
import com.clint.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 添加套餐及其对应菜品信息
     */
    @Override
    @Transactional
    public void saveWithSetmealDish(SetmealDto setmealDto) {
        // 添加套餐
        this.save(setmealDto);

        // 获取添加后的套餐 ID
        Long setmealId = setmealDto.getId();

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        // 补全信息
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        // 添加套餐对应菜品
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据套餐 ID 获取套餐及菜品
     *
     * @param id 套餐 ID
     */
    @Override
    public SetmealDto getByIdWithSetmealDish(Long id) {
        SetmealDto setmealDto = new SetmealDto();

        // 查询套餐信息
        Setmeal setmeal = this.getById(id);
        // 拷贝信息
        BeanUtils.copyProperties(setmeal, setmealDto);

        // 查询菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null, SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    /**
     * 修改套餐信息
     */
    @Override
    @Transactional
    public void update(SetmealDto setmealDto) {
        // 获取套餐 ID
        Long setmealId = setmealDto.getId();

        // 清空套餐对应菜品信息
        LambdaUpdateWrapper<SetmealDish> setmealDishLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        setmealDishLambdaUpdateWrapper.eq(setmealId != null, SetmealDish::getSetmealId, setmealId);
        setmealDishService.remove(setmealDishLambdaUpdateWrapper);

        // 修改套餐信息
        this.updateById(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        // 补全信息
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        // 追加套餐对应菜品信息
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 批量删除套餐
     */
    @Override
    public void removeWithSetmealDish(Long[] ids) {
        for (Long id : ids) {
            // 根据套餐 ID 删除套餐关联的菜品
            LambdaUpdateWrapper<SetmealDish> setmealDishLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            setmealDishLambdaUpdateWrapper.eq(SetmealDish::getSetmealId, id);

            setmealDishService.remove(setmealDishLambdaUpdateWrapper);

            // 删除套餐
            this.removeById(id);
        }
    }
}
