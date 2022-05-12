package com.clint.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clint.reggie.entity.Category;

/**
 * 分类业务层接口
 */
public interface CategoryService extends IService<Category> {

    void removeCategoryById(Long id);

}
