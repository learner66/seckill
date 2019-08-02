package com.zhang.seckill.vo;

import com.zhang.seckill.domain.SeckillUser;

//将订单详情页面需要返回的内容包装起来
public class GoodsDetailVo {
    private int seckillStatus;
    private int remainSeconds;
    private GoodsVo goodsVo;
    private SeckillUser seckillUser;

    public int getSeckillStatus() {
        return seckillStatus;
    }

    public void setSeckillStatus(int seckillStatus) {
        this.seckillStatus = seckillStatus;
    }

    public int getRemainSeconds() {
        return remainSeconds;
    }

    public void setRemainSeconds(int remainSeconds) {
        this.remainSeconds = remainSeconds;
    }

    public GoodsVo getGoodsVo() {
        return goodsVo;
    }

    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }

    public SeckillUser getSeckillUser() {
        return seckillUser;
    }

    public void setSeckillUser(SeckillUser seckillUser) {
        this.seckillUser = seckillUser;
    }
}
