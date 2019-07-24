package com.zhang.seckill.exception;

import com.zhang.seckill.result.CodeMsg;

public class GlobalException extends Exception {

    private CodeMsg cm;
    public GlobalException(CodeMsg cm){
        super(cm.toString());
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }
}
