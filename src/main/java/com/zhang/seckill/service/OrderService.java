package com.zhang.seckill.service;

import com.zhang.seckill.dao.OrderDao;
import com.zhang.seckill.domain.OrderInfo;
import com.zhang.seckill.domain.SeckillOrder;
import com.zhang.seckill.domain.SeckillUser;
import com.zhang.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    OrderDao orderDao;
    public SeckillOrder findOrderByUserIdAndGoodsId(Long userId, long goodsId) {
        return orderDao.findOrderByUserIdAndGoodsId(userId,goodsId);
    }

    @Transactional
    public OrderInfo createOrder(SeckillUser seckillUser, GoodsVo goodsVo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goodsVo.getId());
        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setGoodsPrice(goodsVo.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setUserId(seckillUser.getId());
        orderInfo.setStatus(1);
        orderDao.insert(orderInfo);
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setUserId(seckillUser.getId());
        orderDao.insertSecOrder(seckillOrder);
        return orderInfo;
    }
}
