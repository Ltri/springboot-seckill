package com.ltri.seckill.mapper;

import com.ltri.seckill.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ltri
 * @since 2020-04-18
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    @Update("update goods set stock = stock -1 where stock >0 and id = #{id}")
    void decStock(Long id);

}
