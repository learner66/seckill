package com.zhang.seckill.service;

import com.zhang.seckill.dao.SeckillUserDao;
import com.zhang.seckill.domain.OrderInfo;
import com.zhang.seckill.domain.SeckillUser;
import com.zhang.seckill.exception.GlobalException;
import com.zhang.seckill.redis.RedisService;
import com.zhang.seckill.redis.SeckillUserKey;
import com.zhang.seckill.result.CodeMsg;
import com.zhang.seckill.util.MD5Util;
import com.zhang.seckill.util.UUIDUtil;
import com.zhang.seckill.vo.GoodsVo;
import com.zhang.seckill.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillService {
    public static final String COOKIE_NAME_TOKEN = "token";
    @Autowired
    RedisService redisService;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    SeckillUserDao seckillUserDao;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    //减少订单
    @Transactional
    public  OrderInfo seckill(SeckillUser seckillUser, GoodsVo goodsVo) {
        //1.从秒杀商品表中减少库存
        goodsService.reduceStock(goodsVo);
        //2.生成秒杀商品订单
        OrderInfo orderInfo = orderService.createOrder(seckillUser,goodsVo);
        return orderInfo;
    }

    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)){
            return null;
        }
        SeckillUser user = redisService.get(SeckillUserKey.token,token,SeckillUser.class);
        //延长token
        if(user!=null)
            addCookie(response,user);
        return user;
    }

    public SeckillUser getById(Long id){
        return seckillUserDao.getById(id);
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) throws GlobalException {

        if(loginVo==null){
            //return CodeMsg.SERVER_ERROR;
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        //参数校验
        String mobile = loginVo.getMobile();
        String fromPass = loginVo.getPassword();
        SeckillUser seckillUser =  getById(Long.parseLong(mobile));
        if(seckillUser==null){
            //return CodeMsg.USER_NOT_EXITS;
            throw new GlobalException(CodeMsg.USER_NOT_EXITS);
        }
        //验证密码
        String dbPass = seckillUser.getPassword();
        String salt = seckillUser.getSalt();

        String pass = MD5Util.formPassToDBPass(fromPass,salt);
        if(!pass.equals(dbPass)){
            //return CodeMsg.PASSWORD_ERROR;
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        addCookie(response,seckillUser);
        //生成cookie
       /* Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);*/
        return true;
    }
    private void addCookie(HttpServletResponse response,SeckillUser seckillUser){
        //生成cookie
        String token = UUIDUtil.uuid();
        //用户信息写入到redis中
        redisService.set(SeckillUserKey.token,token,seckillUser);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
