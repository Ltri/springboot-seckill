package com.ltri.seckill.service.impl;

import com.ltri.seckill.entity.Goods;
import com.ltri.seckill.mapper.GoodsMapper;
import com.ltri.seckill.service.IGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ltri
 * @since 2020-04-18
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public void decStock(Long id) {
        goodsMapper.decStock(id);
    }
}
