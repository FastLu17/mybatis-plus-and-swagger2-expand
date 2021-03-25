package com.luxf.mybatis.plus.base;

public class Result<T> {

    private static final int SUCCESS_CODE = 0;
    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";

    private int code;
    private String msg;
    private T data;


    private Result(T data, int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private Result() {
        this.code = SUCCESS_CODE;
        this.msg = SUCCESS;
    }

    private Result(String msg) {
        this.msg = msg;
        this.code = -1;
    }

    public static <T> Result<T> success() {
        return new Result<>();
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, SUCCESS_CODE, SUCCESS);
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
        return this.code == SUCCESS_CODE;
    }
}