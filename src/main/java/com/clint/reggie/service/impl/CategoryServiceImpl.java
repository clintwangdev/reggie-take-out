package com.clint.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clint.reggie.common.CustomException;
import com.clint.reggie.entity.Category;
import com.clint.reggie.entity.Dish;
import com.clint.reggie.entity.Setmeal;
import com.clint.reggie.mapper.CategoryMapper;
import com.clint.reggie.service.CategoryService;
import com.clint.reggie.service.DishService;
import com.clint.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 分类业务层实现
 */
@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据 ID 删除分类
     */
    @Override
    public void removeCategoryById(Long id) {
        log.info("删除分类信息");
        // 在删除之前，先验证分类信息是否关联了'套餐'和'菜品'
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishLambdaQueryWrapper);

        if (dishCount > 0) {
            // 如果关联了'菜品'信息，抛出一个业务异常
            throw new CustomException("当前分类项存在关联菜品信息，删除失败。");
        }
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);

        if (setmealCount != 0) {
            // 如果关联了'套餐'信息，抛出一个业务异常
            throw new CustomException("当前分类项存在关联套餐信息，删除失败。");
        }

        // 执行删除分类信息操作
        super.removeById(id);
    }
}
