package com.zhang.seckill.service;


import com.zhang.seckill.dao.GoodsDao;
import com.zhang.seckill.domain.SeckillGoods;
import com.zhang.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsDao goodsDao;
    public List<GoodsVo> findGoodsVo(){
        return goodsDao.getGoodsVo();
    }

    public GoodsVo getGoodsByGoodsId(long goodsId) {
        return goodsDao.getGoodsByGoodsID(goodsId);
    }

    public int reduceStock(GoodsVo goodsVo){
        SeckillGoods goods = new SeckillGoods();
        goods.setGoodsId(goodsVo.getId());
        return goodsDao.reduceStock(goods);
    }
}
