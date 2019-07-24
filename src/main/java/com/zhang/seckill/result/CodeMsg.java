package com.zhang.seckill.result;

public class CodeMsg {
    private int code;
    private String msg;

    //通用异常
    public static CodeMsg SUCESS = new CodeMsg(0,"sucess");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500100,"服务端异常");
    //其他异常信息
     CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
