package com.clint.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clint.reggie.common.R;
import com.clint.reggie.entity.Category;
import com.clint.reggie.dto.PageDto;
import com.clint.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("新增分类");
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询分类列表
     */
    @GetMapping("/page")
    public R<Page<Category>> page(PageDto pageVo) {
        log.info("分页查询分类列表");
        // 构建分页构造器
        Page<Category> categoryPage = new Page<>(pageVo.getPage(), pageVo.getPageSize());
        // 执行分页查询
        categoryService.page(categoryPage);

        return R.success(categoryPage);
    }

    /**
     * 根据 ID 删除分类
     */
    @DeleteMapping
    public R<String> remove(Long id) {
        log.info("删除分类信息");
        categoryService.removeCategoryById(id);
        return R.success("删除分类信息成功");
    }

    /**
     * 修改分类信息
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息");
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 回显分类信息
     */
    @GetMapping("/{id}")
    public R<Category> getCategoryById(@PathVariable Long id) {
        log.info("根据 ID 查询分类信息");
        Category category = categoryService.getById(id);
        return R.success(category);
    }

    /**
     * 获取菜品分类
     *
     * @param type 分类类型 (1:菜品分类 / 2:套餐分类)
     * @return 菜品分类列表
     */
    @GetMapping("/list")
    public R<List<Category>> getCategoryByType(Integer type) {
        log.info("获取所有菜品分类: " + type);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(type != null, Category::getType, type);
        List<Category> categories = categoryService.list(queryWrapper);
        return R.success(categories);
    }

}
