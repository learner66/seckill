package com.zhang.seckill.redis;

public class GoodsKey extends BasePrefix {
    private   final  static  int GOODS_EXPIRE = 60;
    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static GoodsKey goodsList= new GoodsKey(GOODS_EXPIRE, "goodsList");
    public static GoodsKey goodsId= new GoodsKey(GOODS_EXPIRE, "goodsId");
}
