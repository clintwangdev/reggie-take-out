package com.clint.reggie.common;

/**
 * 基于 ThreadLocal 的工具类，用于保存和获取当前登录用户 ID
 */
public class BaseContext {

    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 保存当前登录用户 ID
     * @param id
     */
    public static void setCurrentId(Long id) {
        THREAD_LOCAL.set(id);
    }


    /**
     * 获取当前登录用户 ID
     */
    public static long getCurrentId() {
        return THREAD_LOCAL.get();
    }
}
