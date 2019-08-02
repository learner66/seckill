package com.zhang.seckill.controller;

import com.zhang.seckill.domain.OrderInfo;
import com.zhang.seckill.domain.SeckillOrder;
import com.zhang.seckill.domain.SeckillUser;
import com.zhang.seckill.rabbitmq.MQSender;
import com.zhang.seckill.rabbitmq.SeckillMessage;
import com.zhang.seckill.redis.GoodsKey;
import com.zhang.seckill.redis.RedisService;
import com.zhang.seckill.result.CodeMsg;
import com.zhang.seckill.result.Result;
import com.zhang.seckill.service.GoodsService;
import com.zhang.seckill.service.OrderService;
import com.zhang.seckill.service.SeckillService;
import com.zhang.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    SeckillService seckillService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    @Autowired
    RedisService redisService;
    @Autowired
    MQSender mqSender;
    //标记库存是否已经没有了。
    private HashMap<Long,Boolean> localOverMap = new HashMap();

    //加载商品库存信息到缓存中
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.findGoodsVo();
        if(goodsVoList==null){
            return ;
        }
        //将库存信息存储到缓存中
        for(GoodsVo goodsVo:goodsVoList){
            redisService.set(GoodsKey.goodsStock,""+goodsVo.getId(),goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(),false);
        }
    }

    @RequestMapping(value = "/{path}/do_seckill",method = RequestMethod.POST)
    @ResponseBody
    public Result<CodeMsg> do_seckill(HttpServletResponse response,Model model, @RequestParam("goodsId")long goodsId,
     @CookieValue(value= SeckillService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
                             @RequestParam(value=SeckillService.COOKIE_NAME_TOKEN,required = false) String paramToken,
                                      @PathVariable("path")String path){
        //首先检查用户是否存在，用户不存在要返回登陆页面,用户应该从缓存中查找
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            //return "login";
            return Result.error(CodeMsg.USER_NOT_EXITS);
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken:paramToken;
        SeckillUser seckillUser = seckillService.getByToken(response,token);
        model.addAttribute("seckillUser",seckillUser);

        if(seckillUser==null){
            return Result.error(CodeMsg.USER_NOT_EXITS);
        }

        boolean check = seckillService.checkPath(seckillUser,goodsId,path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        boolean over =  localOverMap.get(goodsId);
        if(over){
            return Result.error(CodeMsg.STOCK_EMPTY);
        }

        //预减库存，从redis缓存中获得库存信息
        //long stock = redisService.get(GoodsKey.goodsStock,""+goodsId,Long.class);
        long stock = redisService.decr(GoodsKey.goodsStock,""+goodsId);
        if(stock<0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.STOCK_EMPTY);
        }
        //判断是否已经秒杀到了
        SeckillOrder  seckillOrder = orderService.findOrderByUserIdAndGoodsId(seckillUser.getId(),goodsId);
        if(seckillOrder!=null){
            //model.addAttribute("msg", CodeMsg.ORDER_UNREAPTEABLE.getMsg());
            //return "seckill_fail";
            return Result.error(CodeMsg.ORDER_UNREAPTEABLE);
        }

        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setGoodsId(goodsId);
        seckillMessage.setSeckillUser(seckillUser);
        mqSender.sendMessage(seckillMessage);
        return Result.sucess(0);//排队中

        /*//1.如果用户不存在，那么就返回到登陆页面
        if(seckillUser==null){
            //return "login";
            return  Result.error(CodeMsg.USER_NOT_EXITS);
        }

        //2.判断存库，应该在秒杀商品表验证
        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        int stock = goodsVo.getGoodsStock();
        if(stock<=0){
           // model.addAttribute("msg", CodeMsg.STOCK_EMPTY.getMsg());
           // return "seckill_fail";
           return Result.error(CodeMsg.STOCK_EMPTY);
        }

        //3.如果该用户已经下过订单(秒杀订单)，那么就不能再进行下单
        SeckillOrder  seckillOrder = orderService.findOrderByUserIdAndGoodsId(seckillUser.getId(),goodsId);
        if(seckillOrder!=null){
            //model.addAttribute("msg", CodeMsg.ORDER_UNREAPTEABLE.getMsg());
            //return "seckill_fail";
            return Result.error(CodeMsg.ORDER_UNREAPTEABLE);

        }

        //4.减库存，生成订单,为用户和商品生成订单，并将库存减少,应该添加事务
        OrderInfo orderInfo = seckillService.seckill(seckillUser,goodsVo);
        //model.addAttribute("orderInfo",orderInfo);
        // model.addAttribute("goods",goodsVo);
        //return "order_detail";*/
       // return Result.sucess(orderInfo);
    }

    /**
     *
     * @param response
     * @param model
     * @param goodsId
     * @param cookieToken
     * @param paramToken
     * @return orderId成功，-1秒杀失败，0排队中
     */
    @RequestMapping("/result")
    @ResponseBody
    public Result<Long> seckillResult(HttpServletResponse response,Model model, @RequestParam("goodsId")long goodsId,
                                      @CookieValue(value= SeckillService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
                                      @RequestParam(value=SeckillService.COOKIE_NAME_TOKEN,required = false) String paramToken){
        //首先检查用户是否存在，用户不存在要返回登陆页面,用户应该从缓存中查找
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            //return "login";
            return Result.error(CodeMsg.USER_NOT_EXITS);
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken:paramToken;
        SeckillUser seckillUser = seckillService.getByToken(response,token);
        model.addAttribute("seckillUser",seckillUser);

        if(seckillUser==null){
            return Result.error(CodeMsg.USER_NOT_EXITS);
        }
        long result = seckillService.getSeckillResult(seckillUser.getId(),goodsId);
        return Result.sucess(result);
    }

    @RequestMapping("/path")
    @ResponseBody
    public Result<Long> path(HttpServletResponse response,@RequestParam("goodsId")long goodsId,
                                      @CookieValue(value= SeckillService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
                                      @RequestParam(value=SeckillService.COOKIE_NAME_TOKEN,required = false) String paramToken) {
        //首先检查用户是否存在，用户不存在要返回登陆页面,用户应该从缓存中查找
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            //return "login";
            return Result.error(CodeMsg.USER_NOT_EXITS);
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken:paramToken;
        SeckillUser seckillUser = seckillService.getByToken(response,token);
        String path = seckillService.createPath(seckillUser,goodsId);
        return Result.sucess(path);
    }

}
