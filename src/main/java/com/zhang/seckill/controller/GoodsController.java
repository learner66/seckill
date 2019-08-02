package com.zhang.seckill.controller;

import com.zhang.seckill.redis.GoodsKey;
import com.zhang.seckill.result.Result;
import com.zhang.seckill.service.GoodsService;
import com.zhang.seckill.service.SeckillService;
import com.zhang.seckill.domain.SeckillUser;
import com.zhang.seckill.redis.RedisService;
import com.zhang.seckill.vo.GoodsDetailVo;
import com.zhang.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    private static Logger log = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    SeckillService seckillService;
    @Autowired
    RedisService redisService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request,HttpServletResponse response, Model model, @CookieValue(value= SeckillService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
                         @RequestParam(value=SeckillService.COOKIE_NAME_TOKEN,required = false) String paramToken){
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return "login";
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken:paramToken;
        SeckillUser user = seckillService.getByToken(response,token);

        /*  这里没有使用缓存，每次渲染一个新页面返回
            List<GoodsVo> goodsList =  goodsService.findGoodsVo();
            model.addAttribute("goodsList",goodsList);
            return "goods_list";
        */
        //使用redis缓存，如果缓存中存在查找的页面内容，直接返回就可以，如果没有，则手动渲染，并把页面存入缓存。
        String html = redisService.get(GoodsKey.goodsList,"",String.class);
        //如果redis缓存中有html，则可以获取该htm
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //如果没有，则手动渲染
        List<GoodsVo> goodsList =  goodsService.findGoodsVo();
        model.addAttribute("goodsList",goodsList);
        /*SpringWebContext ctx = new SpringWebContext(request,response,
                request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );*/
        WebContext wc = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",wc);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.goodsList,"",html);
        }
        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}",produces = "text/html")
    @ResponseBody
    public String detail(HttpServletRequest request,HttpServletResponse response,Model model, SeckillUser seckillUser,
                         @PathVariable("goodsId")long goodsId){
        //snowflake
        model.addAttribute("user",seckillUser);
        //同样，这里也增加缓存
        String html = redisService.get(GoodsKey.goodsId,""+goodsId,String.class);
        if(!StringUtils.isEmpty(html)){
            return  html;
        }

        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        long startTime = goodsVo.getStartDate().getTime();
        long endTime = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int seckillStatus  = 0;
        int remainSeconds = 0;

        if(now<startTime){ //秒杀还没开始，倒计时
            seckillStatus  = 0;
            remainSeconds = (int)((startTime - now)/1000);
        }else if(now>endTime){ //秒杀已经结束
            seckillStatus  = 2;
            remainSeconds = -1;
        }else{ //秒杀进行时
            seckillStatus  = 1;
            remainSeconds = 0;
        }
        model.addAttribute("seckillStatus",seckillStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("goodsVo",goodsVo);

        //缓存中没有，那么就手动渲染，注意，缓存是有时效的
        WebContext wc = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",wc);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.goodsId,""+goodsId,html);
        }
        log.info("html 缓存");
        return html;
        //return "goods_detail";
    }

    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail2(HttpServletRequest request,HttpServletResponse response,Model model, SeckillUser seckillUser,
                         @PathVariable("goodsId")long goodsId){
        //snowflake
        model.addAttribute("user",seckillUser);

        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        long startTime = goodsVo.getStartDate().getTime();
        long endTime = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int seckillStatus  = 0;
        int remainSeconds = 0;

        if(now<startTime){ //秒杀还没开始，倒计时
            seckillStatus  = 0;
            remainSeconds = (int)((startTime - now)/1000);
        }else if(now>endTime){ //秒杀已经结束
            seckillStatus  = 2;
            remainSeconds = -1;
        }else{ //秒杀进行时
            seckillStatus  = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoodsVo(goodsVo);
        vo.setRemainSeconds(remainSeconds);
        vo.setSeckillStatus(seckillStatus);
        vo.setSeckillUser(seckillUser);
        log.info("页面静态化");
        return Result.sucess(vo);
        //return "goods_detail";
    }
}
