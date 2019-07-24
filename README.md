# seckill
秒杀系统实践

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

















