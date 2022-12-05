package com.zkd.robotTrack.utils;

public class UserThreadLocal {

    private static final ThreadLocal<Long> LOCAL = new ThreadLocal<>();

    private UserThreadLocal() {

    }

    /**
     * 将机器人id放入本地线程中
     *
     * @param robotId
     */
    public static void set(Long robotId) {
        LOCAL.set(robotId);
    }

    /**
     * 从本地线程中获取当前的机器人id
     *
     * @return
     */
    public static Long get() {
        return LOCAL.get();
    }

    /**
     * 从本地线程中删除当前的机器人id
     */
    public static void remove() {
        LOCAL.remove();
    }
}
