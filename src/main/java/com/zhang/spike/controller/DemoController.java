package com.zhang.spike.controller;


import com.zhang.spike.domain.User;
import com.zhang.spike.redis.RedisService;
import com.zhang.spike.redis.UserKey;
import com.zhang.spike.result.Result;
import com.zhang.spike.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
public class DemoController {
    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/")
    @ResponseBody
    String home(){
        return "hello world";
    }

    /**输出结果：1.rest api json输出 2.页面
     *
     *  {
     *      'code':'0',
     *      'msg': '库存不足'
     *      'data': 具体数据
     *  }
     * @return
     */

    @RequestMapping("/hello")
    @ResponseBody
    Result<String> helloError(){
        //利用这种方式就可以产生一个符合想要的数据格式，但是每次都要生成Result
        //return new Result(0,"success","hello，zhang");
        return Result.sucess("hello,zhang");
    }

    @RequestMapping("/thymeleaf")
    public String  thymeleaf(Model model) {
        model.addAttribute("name", "Joshua");
        return "hello";
    }

    @RequestMapping("/user")
    @ResponseBody
    public Result<User> user(){
        User user = userService.getById(1);
        return Result.sucess(user);
    }

    @Transactional
    @RequestMapping("/add")
    @ResponseBody
    public boolean add(){
        User user = new User();
        user.setId(3);
        user.setName("zhang22");
        userService.Insert(user);
        user.setId(1);
        user.setName("zhang");
        userService.Insert(user);
        user.setId(2);
        user.setName("zhang22");
        userService.Insert(user);
        return true;
    }

    @RequestMapping("/jedis")
    @ResponseBody
    public Result<String> jedisService(){
        //利用前缀来分辨不同的功能
       User user =  redisService.get(UserKey.getById,"key1",User.class);
       return Result.sucess(user);
    }

    @RequestMapping("/jedis2")
    @ResponseBody
    public Result<String> jedisService2(){
        User user = new User();
        user.setId(1);
        user.setName("haha");
        redisService.set(UserKey.getById,"key1",user);
        return Result.sucess("sucess");
    }

}
