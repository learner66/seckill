package com.zhang.seckill.rabbitmq;

import com.zhang.seckill.domain.SeckillUser;

public class SeckillMessage {
    private SeckillUser seckillUser;
    private Long goodsId;

    public SeckillUser getSeckillUser() {
        return seckillUser;
    }

    public void setSeckillUser(SeckillUser seckillUser) {
        this.seckillUser = seckillUser;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }
}
