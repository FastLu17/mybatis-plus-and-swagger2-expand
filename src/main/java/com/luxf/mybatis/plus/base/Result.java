package com.luxf.mybatis.plus.base;

public class Result<T> {

    private static final int SUCCESS_code = 0;
    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";

    private int code;
    private String msg;
    private T data;


    private Result(T data) {
        this.code = SUCCESS_code;
        this.msg = SUCCESS;
        this.data = data;
    }

    private Result(String msg) {
        this.msg = msg;
        this.code = -1;
    }

    public static <T> Result<T> success() {
        return new Result<>(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    public static <T> Result<T> failed(String msg) {
        return new Result<>(msg);
    }

    public static <T> Result<T> failed() {
        return new Result<>(FAILED);
    }

    public int getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }


    public T getData() {
        return data;
    }


    public boolean isSuccess() {
        return this.code == SUCCESS_code;
    }
}