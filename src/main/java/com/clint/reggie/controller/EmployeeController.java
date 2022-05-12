package com.clint.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clint.reggie.common.R;
import com.clint.reggie.entity.Employee;
import com.clint.reggie.dto.PageDto;
import com.clint.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 员工控制类
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 将密码进行 MD5 加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 根据用户提交的 username 查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 如果员工信息为空
        if (emp == null) {
            return R.error("员工信息不存在，请重试!");
        }
        // 如果密码不一致
        if (!emp.getPassword().equalsIgnoreCase(password)) {
            return R.error("账号或密码错误，登录失败!");
        }
        // 查看员工状态
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用！！！");
        }

        // 登录成功
        request.getSession().setAttribute("employee", emp);
        return R.success(emp);
    }

    /**
     * 员工登出
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("登出成功");
    }

    /**
     * 添加员工
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee) {
        // 设置员工默认密码为 123456 的密文
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        log.info("添加员工，员工信息: {}", employee);

        employeeService.save(employee);
        return R.success("添加员工成功");
    }

    /**
     * 分页多条件查询员工
     */
    @GetMapping("/page")
    public R<Page<Employee>> page(PageDto employeeVo) {
        log.info(employeeVo.toString());

        // 构建分页构造器
        Page<Employee> page = new Page<>(employeeVo.getPage(), employeeVo.getPageSize());

        // 构建条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!StringUtils.isEmpty(employeeVo.getName()), Employee::getName, employeeVo.getName());
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeService.page(page, queryWrapper);

        return R.success(page);
    }

    /**
     * 修改员工信息
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        log.info(employee.toString());
        employeeService.updateById(employee);
        return R.success("修改状态成功");
    }

    /**
     * 回显员工信息
     */
    @GetMapping("/{id}")
    public R<Employee> getEmployeeById(@PathVariable Long id) {
        log.info("根据 ID 查询员工信息");
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }
}
