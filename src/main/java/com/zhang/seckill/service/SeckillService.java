package com.zhang.seckill.service;

import com.zhang.seckill.dao.SeckillUserDao;
import com.zhang.seckill.domain.OrderInfo;
import com.zhang.seckill.domain.SeckillOrder;
import com.zhang.seckill.domain.SeckillUser;
import com.zhang.seckill.exception.GlobalException;
import com.zhang.seckill.redis.RedisService;
import com.zhang.seckill.redis.SeckillKey;
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
        setGoodsOver(goodsVo.getId());
        return orderInfo;
    }

    private void setGoodsOver(long id) {
        redisService.set(SeckillKey.isGoodsOver,""+id,true);
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

    public long getSeckillResult(Long id, long goodsId) {
        SeckillOrder seckillOrder = orderService.findOrderByUserIdAndGoodsId(id,goodsId);
        if(seckillOrder!=null){//秒杀成功
            return seckillOrder.getOrderId();
        }else{
           boolean isOver = getGoodsOver(goodsId);
           if(isOver){
               return -1;
           }else{
               return 0;
           }
        }
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exist(SeckillKey.isGoodsOver,""+goodsId);
    }

    //生成随机路径
    public String createPath(SeckillUser seckillUser, long goodsId) {
        if(seckillUser == null || goodsId <=0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid()+"123456");
        redisService.set(SeckillKey.getSeckillPath, ""+seckillUser.getId() + "_"+ goodsId, str);
        return str;
    }

    //验证请求路径
    public boolean checkPath(SeckillUser seckillUser, long goodsId, String path) {
        if(seckillUser == null || path == null) {
            return false;
        }
        String pathOld = redisService.get(SeckillKey.getSeckillPath, ""+seckillUser.getId() + "_"+ goodsId, String.class);
        return path.equals(pathOld);
    }
}
