package com.clint.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clint.reggie.dto.DishDto;
import com.clint.reggie.entity.Dish;
import com.clint.reggie.entity.DishFlavor;
import com.clint.reggie.mapper.DishMapper;
import com.clint.reggie.service.DishFlavorService;
import com.clint.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 添加菜品及口味信息
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品
        save(dishDto);
        // 获取保存后菜品的 ID
        Long dishId = dishDto.getId();

        // 保存菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 补全信息
        flavors = flavors.stream()
                .peek(dishFlavor -> dishFlavor.setDishId(dishId)).collect(Collectors.toList());
        // 批处理保存菜品口味
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据 ID 查询菜品及其口味
     *
     * @param id 菜品 ID
     * @return 封装菜品及口味的对象
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        // 获取菜品信息
        Dish dish = getById(id);
        // 封装对象
        BeanUtils.copyProperties(dish, dishDto);

        // 获取菜品对应的口味
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 修改菜品及口味
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 修改菜品信息
        updateById(dishDto);

        // 先清空菜品对应的口味
        LambdaUpdateWrapper<DishFlavor> dishFlavorLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        dishFlavorLambdaUpdateWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(dishFlavorLambdaUpdateWrapper);

        // 添加对应的口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 设置菜品 ID
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 批量删除菜品及对应口味
     *
     * @param ids 菜品 ID 数组
     */
    @Override
    public void removeWithFlavor(Long[] ids) {
        for (Long id : ids) {
            // 先删除对应的口味
            LambdaUpdateWrapper<DishFlavor> dishFlavorLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            dishFlavorLambdaUpdateWrapper.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(dishFlavorLambdaUpdateWrapper);

            // 删除菜品
            this.removeById(id);
        }
    }
}
