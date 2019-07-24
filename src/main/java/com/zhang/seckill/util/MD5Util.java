package com.zhang.seckill.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
    public static  String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d"; //1a2b3c4d

    //第一次加密,一般发生在js中
    public static String inputPassFormPass(String inputPass){
        //String str = salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
        String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    //第二次加密
    public static String formPassToDBPass(String formPass,String salt){
        String str = salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    public static String inputPassToDBPass(String input,String salt){
        String formPass = inputPassFormPass(input);
        String dbPass = formPassToDBPass(formPass,salt);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));

        System.out.println(inputPassFormPass("123456"));
    }
}
