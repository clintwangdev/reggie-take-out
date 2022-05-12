package com.clint.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.clint.reggie.common.BaseContext;
import com.clint.reggie.common.R;
import com.clint.reggie.entity.ShoppingCart;
import com.clint.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info(shoppingCart.toString());
        // 设置用户 ID
        shoppingCart.setUserId(BaseContext.getCurrentId());

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        Long dishId = shoppingCart.getDishId();
        // 判断当前菜品或套餐是否已存在于购物车
        if (dishId != null) {
            // 是一个菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            // 是一个套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart spc = shoppingCartService.getOne(queryWrapper);

        if (spc != null) {
            // 购物车中已存在一条该记录
            Integer number = spc.getNumber();
            spc.setNumber(++number);
            // 将数量 +1
            shoppingCartService.updateById(spc);
        } else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            spc = shoppingCart;
        }

        return R.success(spc);
    }

    /**
     * 获取当前用户购物车列表
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("获取当前用户的购物车列表");

        long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);

        return R.success(shoppingCartList);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        long userId = BaseContext.getCurrentId();

        LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShoppingCart::getUserId, userId);

        shoppingCartService.remove(updateWrapper);

        return R.success("清空购物车成功");

    }

    /**
     * 减少购物车中的菜品或套餐
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info(shoppingCart.toString());

        // 判断是套餐还是菜品
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        if (dishId != null) {
            // 是菜品
            queryWrapper.eq(dishId != null, ShoppingCart::getDishId, dishId);
        } else {
            // 是套餐
            queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId,
                    shoppingCart.getSetmealId());
        }

        // 查询购物车中该 菜单/套餐 是否为 1 条数据
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);
        Integer number = shoppingCartOne.getNumber();

        // 如果数据大于 1, 将 number 减 1
        if (number > 1) {
            shoppingCartOne.setNumber(--number);
            shoppingCartService.updateById(shoppingCartOne);
        } else {
            // 将该记录删除
            shoppingCartService.remove(queryWrapper);
        }

        return R.success("减少商品成功");
    }
}
