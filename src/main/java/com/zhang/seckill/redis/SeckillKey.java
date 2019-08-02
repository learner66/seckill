package com.zhang.seckill.redis;

public class SeckillKey extends BasePrefix {

    public SeckillKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillKey isGoodsOver = new SeckillKey(0,"goodOver");
    public static SeckillKey  getSeckillPath = new SeckillKey(10,"path");
}
