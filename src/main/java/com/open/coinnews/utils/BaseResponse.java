package com.open.coinnews.utils;

/**
 * @author guominghai
 * @Description:
 * @Date Created in 15:31 2018/4/20
 */
/**
 * API接口的基础返回类
 *
 * @author XiongNeng
 * @version 1.0
 * @since 2018/1/7
 */
public class BaseResponse<T> {
    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 说明
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    public BaseResponse() {

    }

    public BaseResponse(boolean success, String msg, T data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }
}