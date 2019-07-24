package com.zhang.seckill.result;

public class CodeMsg {
    private int code;
    private String msg;

    //通用异常
    public static CodeMsg SUCESS = new CodeMsg(0,"sucess");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500100,"服务端异常");
    //登陆异常信息
    public static CodeMsg PASS_NOT_NULL = new CodeMsg(500200,"密码不能为空");
    public static CodeMsg MOBILE_NOT_NULL = new CodeMsg(500201,"手机号不能为空");
    public static CodeMsg MOBILE_ERROR_FORMAT = new CodeMsg(500202,"手机号码格式出错");
    public static CodeMsg USER_NOT_EXITS =new CodeMsg(500203,"用户不存在");
    public static final CodeMsg PASSWORD_ERROR = new CodeMsg(500204,"密码错误");
    public static final CodeMsg BIND_ERROR = new CodeMsg(500205,"绑定参数异常：%s"); ;



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

    public CodeMsg fillArgs(Object...args){
         int code  = this.code;
         String message = String.format(this.msg,args);
         return new CodeMsg(code,message);
    }

}
