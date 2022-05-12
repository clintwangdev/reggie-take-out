package com.clint.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.clint.reggie.common.BaseContext;
import com.clint.reggie.common.R;
import com.clint.reggie.entity.Employee;
import com.clint.reggie.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户登录检查过滤器
 */
@Slf4j
@WebFilter(
        filterName = "loginCheckFilter",
        urlPatterns = "/*"
)
public class LoginCheckFilter implements Filter {

    /**
     * 路径匹配器
     */
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();
        log.info("拦截到请求: {}", uri);

        boolean check = LoginCheckFilter.check(uri);

        if (check) {
            // 不需要进行任何操作，直接放行
            log.info("本次请求 {} 不需要处理", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 如果是其它请求，判断是否登录
        if (request.getSession().getAttribute("employee") != null) {
            log.info("登录用户名: {}", ((Employee) request.getSession().getAttribute("employee")).getUsername());
            // 保存当前登录用户 ID
            BaseContext.setCurrentId(((Employee) request.getSession().getAttribute("employee")).getId());
            filterChain.doFilter(request, response);
            return;
        }

         // 如果是其它请求，判断是否登录
        if (request.getSession().getAttribute("user") != null) {
            log.info("登录用户名: {}", ((User) request.getSession().getAttribute("user")).getPhone());
            // 保存当前登录用户 ID
            BaseContext.setCurrentId(((User) request.getSession().getAttribute("user")).getId());
            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户尚未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    public static boolean check(String requestUri) {
        // 定义不需要处理的 uri
        String[] uris = {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        for (String uri : uris) {
            if (ANT_PATH_MATCHER.match(uri, requestUri)) {
                // 如果请求 uri 不需要做任何处理
                return true;
            }
        }
        return false;
    }

}
