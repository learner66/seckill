package com.zhang.seckill.controller;


import com.zhang.seckill.domain.SeckillService;
import com.zhang.seckill.domain.SeckillUser;
import com.zhang.seckill.redis.RedisService;
import com.zhang.seckill.redis.SeckillUserKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    SeckillService seckillService;
    @Autowired
    RedisService redisService;

    @RequestMapping("/to_list")
    public String toList(HttpServletResponse response,Model model, @CookieValue(value= SeckillService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
                         @RequestParam(value=SeckillService.COOKIE_NAME_TOKEN,required = false) String paramToken){
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return "login";
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken:paramToken;
        SeckillUser user = seckillService.getByToken(response,token);
        model.addAttribute("user",user);
        return "goods_list";
    }
}
