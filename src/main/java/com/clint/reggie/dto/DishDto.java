package com.clint.reggie.dto;

import com.clint.reggie.entity.Dish;
import com.clint.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于菜品和口味数据的前端数据封装
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
