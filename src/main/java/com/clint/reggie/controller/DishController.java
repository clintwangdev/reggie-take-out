package com.clint.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clint.reggie.common.R;
import com.clint.reggie.dto.DishDto;
import com.clint.reggie.dto.PageDto;
import com.clint.reggie.dto.SetmealDto;
import com.clint.reggie.entity.Dish;
import com.clint.reggie.entity.DishFlavor;
import com.clint.reggie.service.CategoryService;
import com.clint.reggie.service.DishFlavorService;
import com.clint.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 保存菜品及口味
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        // 定义要删除的分类菜品 key
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        // 删除指定分类菜品缓存
        redisTemplate.delete(key);
        return R.success("保存菜品及口味成功");

    }

    /**
     * 根据菜品名分页查询菜品
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(PageDto pageDto) {
        // 分页构造器
        Page<Dish> dishPage = new Page<>(pageDto.getPage(), pageDto.getPageSize());
        Page<DishDto> dishDtoPage = new Page<>();

        // 条件过滤器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        dishLambdaQueryWrapper.like(pageDto.getName() != null, Dish::getName, pageDto.getName());
        // 添加排序条件
        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(dishPage, dishLambdaQueryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        List<Dish> records = dishPage.getRecords();
        List<DishDto> list = records.stream()
                .map(dish -> {
                    DishDto dishDto = new DishDto();
                    BeanUtils.copyProperties(dish, dishDto);
                    // 获取菜品对应的分类 ID
                    Long categoryId = dish.getCategoryId();
                    // 获取分类名
                    String categoryName = categoryService.getById(categoryId).getName();
                    if (categoryName != null) {
                        dishDto.setCategoryName(categoryName);
                    }
                    return dishDto;
                }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 根据 ID 回显菜品信息
     */
    @GetMapping("/{id}")
    public R<DishDto> getDishById(@PathVariable Long id) {
        log.info("回显菜品信息");

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品及口味信息
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("修改菜品及口味信息");

        dishService.updateWithFlavor(dishDto);

        // 定义要删除的分类菜品 key
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        // 删除指定分类菜品缓存
        redisTemplate.delete(key);

        return R.success("修改成功");
    }

    /**
     * 批量修改菜品状态 (启售/停售)
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, Long[] ids) {
        log.info("修改状态为: " + (status == 0 ? "停售" : "启售") + ", 要修改的菜品 ID: " + Arrays.toString(ids));

        List<Dish> dishList = new ArrayList<>();
        for (Long id : ids) {
            dishList.add(new Dish()
                    .setId(id)
                    .setStatus(status));
        }

        // 批量修改状态
        dishService.updateBatchById(dishList);

        return R.success("修改菜品状态成功");
    }

    /**
     * 批量删除菜品及其口味
     */
    @DeleteMapping
    public R<String> remove(Long[] ids) {
        log.info(Arrays.toString(ids));

        dishService.removeWithFlavor(ids);

        // 清空所有缓存
        Set keys = redisTemplate.keys("dish_*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }

        return R.success("删除菜品成功");
    }

    /**
     * 根据菜品分类 ID 查询对应的菜品数据
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;
        Long categoryId = dish.getCategoryId();
        Integer status = dish.getStatus();
        // 存入 Redis 中的 key
        String key = "dish_" + categoryId + "_" + status;

        // 从缓存中获取菜品数据，验证是否存在
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            // 如果有缓存数据
            return R.success(dishDtoList);
        }
        // 没有缓存数据
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, categoryId);
        queryWrapper.eq(Dish::getStatus, status);

        List<Dish> dishList = dishService.list(queryWrapper);

        dishDtoList = dishList.stream()
                .map(item -> {
                    DishDto dishDto = new DishDto();
                    BeanUtils.copyProperties(item, dishDto);
                    // 获取菜品 ID
                    Long dishId = item.getId();

                    LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
                    List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
                    // 设置口味
                    dishDto.setFlavors(dishFlavorList);
                    return dishDto;
                }).collect(Collectors.toList());

        // 将数据加入缓存, 设置过期时间为 60 分钟
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }

}
