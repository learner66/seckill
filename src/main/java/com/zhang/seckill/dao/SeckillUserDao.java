package com.zhang.seckill.dao;

import com.zhang.seckill.domain.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeckillUserDao {

    @Select("select * from seckilluser where id = #{id}")
    public SeckillUser getById(@Param("id")Long id);

}
