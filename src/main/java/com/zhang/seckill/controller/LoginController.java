package com.zhang.seckill.controller;

import com.zhang.seckill.service.SeckillService;
import com.zhang.seckill.exception.GlobalException;
import com.zhang.seckill.redis.RedisService;
import com.zhang.seckill.result.CodeMsg;
import com.zhang.seckill.result.Result;
import com.zhang.seckill.service.UserService;
import com.zhang.seckill.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {
    private static Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    SeckillService seckillService;

    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) throws GlobalException {
        log.info(loginVo.toString());
        String passinput = loginVo.getPassword();
        String mobile = loginVo.getMobile();
        //密码不能为空
        /*if(StringUtils.isEmpty(passinput)){
            return Result.error(CodeMsg.PASS_NOT_NULL);
        }
        if(StringUtils.isEmpty(mobile)){
            return Result.error(CodeMsg.MOBILE_NOT_NULL);
        }
        if(!ValidatorUtil.isMobile(mobile)){
            return Result.error(CodeMsg.MOBILE_ERROR_FORMAT);
        }*/
        seckillService.login(response,loginVo);
        return Result.sucess(CodeMsg.SUCESS);
    }
}
