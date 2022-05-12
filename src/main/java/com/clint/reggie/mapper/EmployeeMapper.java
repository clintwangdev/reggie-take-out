package com.clint.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clint.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工持久层接口
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
