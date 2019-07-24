package com.zhang.seckill.result;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
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

    private Result(T data){
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    //成功的时候调用
    public static <T> Result sucess(T data){
        return new Result<T>(data);
    }

    private Result(CodeMsg cm){
        if(cm==null){
            return ;
        }
        this.code = cm.getCode();
        this.msg = cm.getMsg();
    }

    //失败的时候调用
    public static <T> Result<T> error(CodeMsg cm){
        return new Result<T>(cm);
    }
}
