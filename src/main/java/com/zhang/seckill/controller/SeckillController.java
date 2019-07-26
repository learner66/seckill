package com.zhang.seckill.controller;

import com.zhang.seckill.domain.OrderInfo;
import com.zhang.seckill.domain.SeckillOrder;
import com.zhang.seckill.domain.SeckillUser;
import com.zhang.seckill.result.CodeMsg;
import com.zhang.seckill.service.GoodsService;
import com.zhang.seckill.service.OrderService;
import com.zhang.seckill.service.SeckillService;
import com.zhang.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    SeckillService seckillService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;

    @RequestMapping("/do_seckill")
    public String do_seckill(HttpServletResponse response,Model model, @RequestParam("goodsId")long goodsId,
     @CookieValue(value= SeckillService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
                             @RequestParam(value=SeckillService.COOKIE_NAME_TOKEN,required = false) String paramToken){
        //首先检查用户是否存在，用户不存在要返回登陆页面,用户应该从缓存中查找
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return "login";
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken:paramToken;

        SeckillUser seckillUser = seckillService.getByToken(response,token);
        model.addAttribute("seckillUser",seckillUser);
        //1.如果用户不存在，那么就返回到登陆页面
        if(seckillUser==null){
            return "login";
        }
        //2.判断存库，应该在秒杀商品表验证
        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        int stock = goodsVo.getGoodsStock();
        if(stock<=0){
            model.addAttribute("msg", CodeMsg.STOCK_EMPTY.getMsg());
            return "seckill_fail";
        }

        //3.如果该用户已经下过订单(秒杀订单)，那么就不能再进行下单
        SeckillOrder  seckillOrder = orderService.findOrderByUserIdAndGoodsId(seckillUser.getId(),goodsId);
        if(seckillOrder!=null){
            model.addAttribute("msg", CodeMsg.ORDER_UNREAPTEABLE.getMsg());
            return "seckill_fail";
        }

        //4.减库存，生成订单,为用户和商品生成订单，并将库存减少,应该添加事务
        OrderInfo orderInfo = seckillService.seckill(seckillUser,goodsVo);
        model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goodsVo);
        return "order_detail";
    }

}
