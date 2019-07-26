package com.zhang.seckill.dao;


import com.zhang.seckill.domain.SeckillGoods;
import com.zhang.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {

    @Select("select goods.*,g.seckill_price,g.stock_count,g.start_date,g.end_date from seckill_goods as g left join goods on g.goods_id = goods.id")
    public List<GoodsVo> getGoodsVo();

    @Select("select goods.*,g.seckill_price,g.stock_count,g.start_date,g.end_date from seckill_goods as g left join goods on g.goods_id = goods.id where goods.id=#{goodsId}")
    GoodsVo getGoodsByGoodsID(@Param("goodsId") long goodsId);

    @Update("update seckill_goods set stock_count = stock_count -1 where goods_id = #{goodsId}")
    int reduceStock(SeckillGoods goods);
}
