# seckill
秒杀系统实践

第一节

1.使用springboot来构造项目，相比于ssm，springboot省略了那些配置文件。

    @SpringBootApplication
    public class SeckillApplication {
        public static void main(String[] args) {
            SpringApplication.run(SeckillApplication.class, args);
        }
    }

2.springboot集成mybatis来进行数据库的操作，并且结合druid来完成数据库的连接。

    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>1.3.0</version>
    </dependency>

    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.11</version>
    </dependency>

    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>1.1.9</version>
    </dependency>

3.使用redis来做缓存，并在Java中使用jedis来操作

    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>1.2.38</version>
    </dependency>

redis这里不细说，需要另外学习。

在Java中操作redis的思路如下：

#1 为了更好的使用jedis，可以自己封装一个jedisService来操作，更方便进行对象的存储。

    public class RedisService {
        @Autowired
        JedisPool jedisPool;

       public <T> T get(KeyPrefix prefix,String key,Class<T> clazz){
          Jedis jedis = null;
          try{
              jedis = jedisPool.getResource();
              String realkey = prefix.getPrefix()+key;
              //获取真正的key
              String str = jedis.get(realkey);
              T t  = stringToBean(str,clazz);
              return t;


          }finally {
              returnToPool(jedis);
          }
       }


    //获取数据
    public <T> boolean set(KeyPrefix prefix,String key,T value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            //String str = jedis.get(key);
            //将数据对象转换为String对象
            String str = beanToString(value);
            if(str==null||str.length()<0){
                return false;
            }
            //为key添加前缀，让不同的模块有不同的key，防止覆盖
            String realkey = prefix.getPrefix()+key;
            int seconds = prefix.expireSeconds();
            //如果小于等于0，则认为key永远有效
            if(seconds<=0){
                jedis.set(realkey,str);
            }else{
                jedis.setex(realkey,seconds,str);
            }
            return true;


        }finally {
            returnToPool(jedis);
        }
    }
#2  对于key值，尽量设计为与用户相关的key，一般需要加上前缀来区分。

    public class BasePrefix  implements KeyPrefix{
        private  int expireSeconds;
        private String prefix;
        @Override
        public int expireSeconds() { //默认0代表永不过期
            return expireSeconds;
        }
        public BasePrefix(String prefix){
            this(0,prefix);
        }
        public BasePrefix(int expireSeconds, String prefix) {
            this.expireSeconds = expireSeconds;
            this.prefix = prefix;
        }
        @Override
        public String getPrefix() {
            String className = getClass().getSimpleName();
            return className+":"+prefix;
        }
    }
#3 对于value值，需要将对象进行json化。

    private <T> String beanToString(T value) {
        if(value==null){
            return null;
        }
        Class<?> clazz = value.getClass();
        if(clazz==int.class||clazz==Integer.class){
            return ""+value;
        }else if(clazz==String.class){
            return (String) value;
        }else if(clazz==long.class||clazz==Long.class){
            return ""+value;
        }else{
            //TODO
            return JSON.toJSONString(value);
        }
    }


    private <T> T stringToBean(String str,Class<T> clazz) {
        if(str==null||str.length()<=0||clazz==null){
            return null;
        }
        if(clazz==int.class||clazz==Integer.class){
            return (T)Integer.valueOf(str);
        }else if(clazz==String.class){
            return (T)str;
        }else if(clazz==long.class||clazz==Long.class){
            return (T) Long.valueOf(str);
        }else{
            //TODO
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }



第二节


1.两次MD5

#1.用户端： PASS= MD5(明文+固定salt)

    function doLogin(){
       g_showLoading();

       var inputPass = $("#password").val();
       var salt = g_passsword_salt;
       var str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
       var password = md5(str);

       $.ajax({
          url: "/login/do_login",
           type: "POST",
           data:{
              mobile:$("#mobile").val(),
              password: password
           },
           success:function(data){
              layer.closeAll();
              if(data.code == 0){
                 layer.msg("成功");
                 window.location.href="/goods/to_list";
              }else{
                 layer.msg(data.msg);
              }
           },
           error:function(){
              layer.closeAll();
           }
       });
    }
#2.服务端： PASS= MD5(用户输入+随机Salt)

    //第一次加密,一般发生在js中
        public static String inputPassFormPass(String inputPass){
            //String str = salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
            String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
            return md5(str);
        }


    //第二次加密
        public static String formPassToDBPass(String formPass,String salt){
            String str = salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
            return md5(str);
        }


    public static String inputPassToDBPass(String input,String salt){
        String formPass = inputPassFormPass(input);
        String dbPass = formPassToDBPass(formPass,salt);
        return dbPass;
    }


2.JR303验证

    @NotNull
    @isMobile
    private String mobile;
    @NotNull
    @Length(min=32)
    private String password;
自定义验证

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    //@Repeatable(javax.validation.constraints.NotNull.List.class)
    @Documented
    @Constraint(
            validatedBy = {isMobileValidator.class}
    )
    public @interface isMobile {
        boolean required() default true;
        String message() default "手机号码格式有误";
        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
        @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        public @interface List {
            javax.validation.constraints.NotNull[] value();
        }
    }
利用ConstraintValidator实际完成验证

    public class isMobileValidator implements ConstraintValidator<isMobile, String> {

        private boolean required = false;
        @Override
        public void initialize(isMobile constraintAnnotation) {
            required = constraintAnnotation.required();
        }
        @Override
        public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
            if(required){ //如果是必需的，那么可以直接判断
                return ValidatorUtil.isMobile(s);
            }else{  //如果不是必需的，那么需要先判空。
                if(StringUtils.isEmpty(s)){
                    return true;
                }else{
                    return ValidatorUtil.isMobile(s);
                }
            }
        }
    }


3.全局异常处理

    @ControllerAdvice
    @ResponseBody
    public class GlobleExceptionHandler {
        @ExceptionHandler(value=Exception.class)
        public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
            if(e instanceof BindException){
                BindException ex = (BindException)e;
                List<ObjectError> errors = ex.getAllErrors();
                ObjectError error = errors.get(0);
                String message = error.getDefaultMessage();
                return Result.error(CodeMsg.BIND_ERROR.fillArgs(message));
            }else{
                return Result.error(CodeMsg.SERVER_ERROR);
            }
        }
    }


4.分布式session：将session存放在redis中

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



第三节：秒杀逻辑
数据库设计

秒杀逻辑（未优化）
#1 进入商品列表页面
#2 点击商品详情页面
#3 点击秒杀
#4 去库存，创建订单

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

使用Jmeter对未优化的代码进行压测

压测的代码段

    @RequestMapping("/to_list")
    public String toList(HttpServletResponse response,Model model, @CookieValue(value= SeckillService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
                         @RequestParam(value=SeckillService.COOKIE_NAME_TOKEN,required = false) String paramToken){
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return "login";
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken:paramToken;
        SeckillUser user = seckillService.getByToken(response,token); //从redis中获取用户缓存

        List<GoodsVo> goodsList =  goodsService.findGoodsVo(); //从数据库中获取商品信息
        model.addAttribute("goodsList",goodsList);
        //log.info("now...");
        return "goods_list";
    }

压测时mysql服务进程的资源利用率

        PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND   
        7715 mysql     20   0 1575008 150028  17692 S 112.3  3.7   0:15.72 mysqld  
        
压测的吞吐量 2439.0/sec

第四节:缓存优化

缓存是为了减轻数据库访问的压力
#1页面缓存：当访问一个页面的时候，可以把该页面放置到redis数据中，方便访问，如果第一次访问的时候，不存在页面缓存，就从数据库中取出数据进行缓存。

    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request,HttpServletResponse response, Model model, @CookieValue(value=                                            SeckillService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
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
#2 URL缓存：当访问一个url的时候，可以根据该url中提供的参数进行缓存，如缓存某一个商品ID的信息。

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
    return html;
    //return "goods_detail";
}


#3 对象缓存：就是当一个访问一个对象的时候，首先从缓存中查找该对象，如果不存在才会去数据库中查找；如果要进行对象的更新操作的话，要注意缓存的更新操作。

    127.0.0.1:6379> get GoodsKey:goodsList
    "<!DOCTYPE HTML>\r\n<html>\r\n<head>\r\n    <title>\xe5\x95\x86\xe5\x93\x81\xe5\x88\x97\xe8\xa1\xa8</title>\r\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\r\n    <!-- jquery -->\r\n    <script type=\"text/javascript\" src=\"/js/jquery.min.js\"></script>\r\n    <!-- bootstrap -->\r\n    <link rel=\"stylesheet\" type=\"text/css\" href=\"/bootstrap/css/bootstrap.min.css\" />\r\n    <script type=\"text/javascript\" src=\"/bootstrap/js/bootstrap.min.js\"></script>\r\n    <!-- jquery-validator -->\r\n    <script type=\"text/javascript\" src=\"/jquery-validation/jquery.validate.min.js\"></script>\r\n    <script type=\"text/javascript\" src=\"/jquery-validation/localization/messages_zh.min.js\"></script>\r\n    <!-- layer -->\r\n    <script type=\"text/javascript\" src=\"/layer/layer.js\"></script>\r\n    <!-- md5.js -->\r\n    <script type=\"text/javascript\" src=\"/js/md5.min.js\"></script>\r\n    <!-- common.js -->\r\n    <script type=\"text/javascript\" src=\"/js/common.js\"></script>\r\n</head>\r\n<body>\r\n<div class=\"panel panel-default\">\r\n    <div class=\"panel-heading\">\xe7\xa7\x92\xe6\x9d\x80\xe5\x95\x86\xe5\x93\x81\xe5\x88\x97\xe8\xa1\xa8</div>\r\n    <table class=\"table\" id=\"goodslist\">\r\n        <tr><td>\xe5\x95\x86\xe5\x93\x81\xe5\x90\x8d\xe7\xa7\xb0</td><td>\xe5\x95\x86\xe5\x93\x81\xe5\x9b\xbe\xe7\x89\x87</td><td>\xe5\x95\x86\xe5\x93\x81\xe5\x8e\x9f\xe4\xbb\xb7</td><td>\xe7\xa7\x92\xe6\x9d\x80\xe4\xbb\xb7</td><td>\xe5\xba\x93\xe5\xad\x98\xe6\x95\xb0\xe9\x87\x8f</td><td>\xe8\xaf\xa6\xe6\x83\x85</td></tr>\r\n        <tr>\r\n            <td>iphonex</td>\r\n            <td ><img src=\"/img/iphonex.png\" width=\"100\" height=\"100\" /></td>\r\n            <td>8756.0</td>\r\n            <td>0.01</td>\r\n            <td>3</td>\r\n            <td><a href=\"/goods/to_detail/1\">\xe8\xaf\xa6\xe6\x83\x85</a></td>\r\n        </tr>\r\n        <tr>\r\n            <td>\xe5\x8d\x8e\xe4\xb8\xbamate10</td>\r\n            <td ><img src=\"/img/meta10.png\" width=\"100\" height=\"100\" /></td>\r\n            <td>3212.0</td>\r\n            <td>0.01</td>\r\n            <td>9</td>\r\n            <td><a href=\"/goods/to_detail/2\">\xe8\xaf\xa6\xe6\x83\x85</a></td>\r\n        </tr>\r\n    </table>\r\n</div>\r\n</body>\r\n</html>\r\n"
    127.0.0.1:6379> get GoodsKey:goodsId1
    "<!DOCTYPE HTML>\r\n<html>\r\n<head>\r\n    <title>\xe5\x95\x86\xe5\x93\x81\xe8\xaf\xa6\xe6\x83\x85</title>\r\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\r\n    <!-- jquery -->\r\n    <script type=\"text/javascript\" src=\"/js/jquery.min.js\"></script>\r\n    <!-- bootstrap -->\r\n    <link rel=\"stylesheet\" type=\"text/css\" href=\"/bootstrap/css/bootstrap.min.css\" />\r\n    <script type=\"text/javascript\" src=\"/bootstrap/js/bootstrap.min.js\"></script>\r\n    <!-- jquery-validator -->\r\n    <script type=\"text/javascript\" src=\"/jquery-validation/jquery.validate.min.js\"></script>\r\n    <script type=\"text/javascript\" src=\"/jquery-validation/localization/messages_zh.min.js\"></script>\r\n    <!-- layer -->\r\n    <script type=\"text/javascript\" src=\"/layer/layer.js\"></script>\r\n    <!-- md5.js -->\r\n    <script type=\"text/javascript\" src=\"/js/md5.min.js\"></script>\r\n    <!-- common.js -->\r\n    <script type=\"text/javascript\" src=\"/js/common.js\"></script>\r\n</head>\r\n<body>\r\n\r\n<div class=\"panel panel-default\">\r\n  <div class=\"panel-heading\">\xe7\xa7\x92\xe6\x9d\x80\xe5\x95\x86\xe5\x93\x81\xe8\xaf\xa6\xe6\x83\x85</div>\r\n  <div class=\"panel-body\">\r\n  \t\r\n  \t<span>\xe6\xb2\xa1\xe6\x9c\x89\xe6\x94\xb6\xe8\xb4\xa7\xe5\x9c\xb0\xe5\x9d\x80\xe7\x9a\x84\xe6\x8f\x90\xe7\xa4\xba\xe3\x80\x82\xe3\x80\x82\xe3\x80\x82</span>\r\n  </div>\r\n  <table class=\"table\" id=\"goodslist\">\r\n  \t<tr>  \r\n        <td>\xe5\x95\x86\xe5\x93\x81\xe5\x90\x8d\xe7\xa7\xb0</td>  \r\n        <td colspan=\"3\">iphonex</td>\r\n     </tr>  \r\n     <tr>  \r\n        <td>\xe5\x95\x86\xe5\x93\x81\xe5\x9b\xbe\xe7\x89\x87</td>  \r\n        <td colspan=\"3\"><img src=\"/img/iphonex.png\" width=\"200\" height=\"200\" /></td>\r\n     </tr>\r\n     <tr>  \r\n        <td>\xe7\xa7\x92\xe6\x9d\x80\xe5\xbc\x80\xe5\xa7\x8b\xe6\x97\xb6\xe9\x97\xb4</td>  \r\n        <td>2019-07-25 10:22:15</td>\r\n        <td id=\"miaoshaTip\">\t\r\n        \t<input type=\"hidden\" id=\"remainSeconds\" value=\"0\" />\r\n        \t\r\n        \t<span>\xe7\xa7\x92\xe6\x9d\x80\xe8\xbf\x9b\xe8\xa1\x8c\xe4\xb8\xad</span>\r\n        \t\r\n        </td>\r\n        <td>\r\n        \t<form id=\"miaoshaForm\" method=\"post\" action=\"/seckill/do_seckill\">\r\n        \t\t<button class=\"btn btn-primary btn-block\" type=\"submit\" id=\"buyButton\">\xe7\xab\x8b\xe5\x8d\xb3\xe7\xa7\x92\xe6\x9d\x80</button>\r\n        \t\t<input type=\"hidden\" name=\"goodsId\" value=\"1\" />\r\n        \t</form>\r\n        </td>\r\n     </tr>\r\n     <tr>  \r\n        <td>\xe5\x95\x86\xe5\x93\x81\xe5\x8e\x9f\xe4\xbb\xb7</td>  \r\n        <td colspan=\"3\">8756.0</td>\r\n     </tr>\r\n      <tr>  \r\n        <td>\xe7\xa7\x92\xe6\x9d\x80\xe4\xbb\xb7</td>  \r\n        <td colspan=\"3\">0.01</td>\r\n     </tr>\r\n     <tr>  \r\n        <td>\xe5\xba\x93\xe5\xad\x98\xe6\x95\xb0\xe9\x87\x8f</td>  \r\n        <td colspan=\"3\">3</td>\r\n     </tr>\r\n  </table>\r\n</div>\r\n</body>\r\n<script>\r\n$(function(){\r\n\tcountDown();\r\n});\r\n\r\nfunction countDown(){\r\n\tvar remainSeconds = $(\"#remainSeconds\").val();\r\n\tvar timeout;\r\n\tif(remainSeconds > 0){//\xe7\xa7\x92\xe6\x9d\x80\xe8\xbf\x98\xe6\xb2\xa1\xe5\xbc\x80\xe5\xa7\x8b\xef\xbc\x8c\xe5\x80\x92\xe8\xae\xa1\xe6\x97\xb6\r\n\t\t$(\"#buyButton\").attr(\"disabled\", true);\r\n\t\ttimeout = setTimeout(function(){\r\n\t\t\t$(\"#countDown\").text(remainSeconds - 1);\r\n\t\t\t$(\"#remainSeconds\").val(remainSeconds - 1);\r\n\t\t\tcountDown();\r\n\t\t},1000);\r\n\t}else if(remainSeconds == 0){//\xe7\xa7\x92\xe6\x9d\x80\xe8\xbf\x9b\xe8\xa1\x8c\xe4\xb8\xad\r\n\t\t$(\"#buyButton\").attr(\"disabled\", false);\r\n\t\tif(timeout){\r\n\t\t\tclearTimeout(timeout);\r\n\t\t}\r\n\t\t$(\"#miaoshaTip\").html(\"\xe7\xa7\x92\xe6\x9d\x80\xe8\xbf\x9b\xe8\xa1\x8c\xe4\xb8\xad\");\r\n\t}else{//\xe7\xa7\x92\xe6\x9d\x80\xe5\xb7\xb2\xe7\xbb\x8f\xe7\xbb\x93\xe6\x9d\x9f\r\n\t\t$(\"#buyButton\").attr(\"disabled\", true);\r\n\t\t$(\"#miaoshaTip\").html(\"\xe7\xa7\x92\xe6\x9d\x80\xe5\xb7\xb2\xe7\xbb\x8f\xe7\xbb\x93\xe6\x9d\x9f\");\r\n\t}\r\n}\r\n\r\n</script>\r\n</html>\r\n"



































