package com.clint.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clint.reggie.common.R;
import com.clint.reggie.entity.User;
import com.clint.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {
        String phone = map.get("phone");
        // 判断是否该用户是否存在，不存在则自动注册
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(phone != null, User::getPhone, phone);

        User user = userService.getOne(queryWrapper);
        session.setAttribute("user", user);

        if (user == null) {
            // 如果不存在，注册用户
            User user1 = new User();
            userService.save(user1.setPhone(phone));
            session.setAttribute("user", user1);
            return R.success(user1);
        }

        return R.success(user);
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session) {
        session.removeAttribute("user");
        return R.success("退出登录成功");
    }
}
