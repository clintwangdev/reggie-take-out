package com.clint.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clint.reggie.common.R;
import com.clint.reggie.dto.PageDto;
import com.clint.reggie.dto.SetmealDto;
import com.clint.reggie.entity.Dish;
import com.clint.reggie.entity.Setmeal;
import com.clint.reggie.entity.SetmealDish;
import com.clint.reggie.service.CategoryService;
import com.clint.reggie.service.DishService;
import com.clint.reggie.service.SetmealDishService;
import com.clint.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    /**
     * 根据套餐名分页查询套餐
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(PageDto pageDto) {
        log.info(pageDto.toString());

        Page<Setmeal> setmealPage = new Page<>(pageDto.getPage(), pageDto.getPageSize());

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(pageDto.getName() != null, Setmeal::getName, pageDto.getName());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        // 执行分页查询操作
        setmealService.page(setmealPage, queryWrapper);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        // 将 setmealPage 拷贝到 setmealDtoPage (排除掉 records 属性)
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> setmealDtos = records.stream()
                .map(setmeal -> {
                    SetmealDto setmealDto = new SetmealDto();
                    BeanUtils.copyProperties(setmeal, setmealDto);

                    // 根据 categoryId 查询套餐对应的分类名
                    Long categoryId = setmeal.getCategoryId();
                    String categoryName = categoryService.getById(categoryId).getName();

                    // 将分类名赋值给每一个 setmealDto 对象
                    if (categoryName != null) {
                        setmealDto.setCategoryName(categoryName);
                    }

                    return setmealDto;
                }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtos);

        return R.success(setmealDtoPage);
    }

    /**
     * 添加套餐及其菜品信息
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());

        setmealService.saveWithSetmealDish(setmealDto);

        return R.success("添加套餐成功");
    }

    /**
     * 回显套餐信息
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getByIdWithSetmealDish(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithSetmealDish(id);

        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());

        setmealService.update(setmealDto);

        return R.success("修改套餐信息成功");
    }

    /**
     * 批量‘停售’/‘启售’ 套餐
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        List<Setmeal> setmeals = new LinkedList<>();
        for (Long id : ids) {
            setmeals.add(new Setmeal()
                    .setStatus(status)
                    .setId(id));
        }
        // 批量根据套餐 ID 修改
        setmealService.updateBatchById(setmeals);

        return R.success("修改套餐状态成功");
    }

    /**
     * 批量删除套餐
     */
    @DeleteMapping
    public R<String> remove(Long[] ids) {
        setmealService.removeWithSetmealDish(ids);

        return R.success("删除套餐成功");
    }

    /**
     * 根据套餐分类 ID 获取套餐
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(queryWrapper);

        return R.success(setmealList);
    }

    /**
     * 根据套餐 ID 获取对应的菜品
     */
    @GetMapping("/dish/{id}")
    public R<List<Dish>> dish(@PathVariable Long id) {
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        queryWrapper.orderByDesc(SetmealDish::getUpdateTime);

        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        List<Dish> dishList = new LinkedList<>();
        // 获取套餐对应菜品信息
        for (SetmealDish setmealDish : setmealDishList) {
            LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishLambdaQueryWrapper.eq(Dish::getId, setmealDish.getDishId());
            Dish dish = dishService.getOne(dishLambdaQueryWrapper);
            dishList.add(dish);
        }

        return R.success(dishList);
    }
}
