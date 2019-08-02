package com.zhang.seckill.rabbitmq;


import com.zhang.seckill.domain.SeckillOrder;
import com.zhang.seckill.domain.SeckillUser;
import com.zhang.seckill.redis.RedisService;
import com.zhang.seckill.service.GoodsService;
import com.zhang.seckill.service.OrderService;
import com.zhang.seckill.service.SeckillService;
import com.zhang.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
    @Autowired
    RedisService redisService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    @Autowired
    SeckillService seckillService;

    @RabbitListener(queues=MQConfig.QUEUE)
    public  void receive(String message){
        log.info("receive message:"+message);
        SeckillMessage seckillMessage  = redisService.stringToBean(message,SeckillMessage.class);
        SeckillUser seckillUser = seckillMessage.getSeckillUser();
        long goodsId = seckillMessage.getGoodsId();
        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        int stock = goodsVo.getStockCount();
        if(stock<=0){
            return ;
        }
        //判断是否已经秒杀到了
        SeckillOrder seckillOrder = orderService.findOrderByUserIdAndGoodsId(seckillUser.getId(),goodsId);
        if(seckillOrder!=null){
           return;
        }
        //减库存，下订单，写入秒杀订单
        seckillService.seckill(seckillUser,goodsVo);
    }
}
