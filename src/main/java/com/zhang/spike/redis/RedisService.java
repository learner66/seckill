package com.zhang.spike.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {
    @Autowired
    JedisPool jedisPool;

    //添加数据
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

    //查询数据是否存在
    public boolean exist(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.exists(realKey);

        }finally {
            returnToPool(jedis);
        }
    }

    //增加值
    public<T> Long incr(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.incr(realKey);

        }finally {
            returnToPool(jedis);
        }
    }

    //减少值
    public<T> Long cr(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            return jedis.decr(realKey);

        }finally {
            returnToPool(jedis);
        }
    }

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

    private void returnToPool(Jedis jedis) {
        if(jedis!=null){
            jedis.close();
        }
    }
}
