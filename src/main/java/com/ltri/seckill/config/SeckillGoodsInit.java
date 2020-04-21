package com.ltri.seckill.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.ltri.seckill.constant.Constant;
import com.ltri.seckill.entity.Goods;
import com.ltri.seckill.service.IGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author ltri
 * @date 2020/4/18 5:20 下午
 */
@Component
@Slf4j
public class SeckillGoodsInit {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static BloomFilter<Long> goodsBloomFilter;

    @PostConstruct
    public void init() {
        List<Goods> goods = goodsService.list();
        goodsBloomFilter = BloomFilter.create(Funnels.longFunnel(), goods.size());
        for (Goods good : goods) {
            stringRedisTemplate.opsForValue().set(Constant.GOODS_STOCK_PRE + good.getId(), good.getStock().toString());
            goodsBloomFilter.put(good.getId());
            log.info("库存预热 id{}", good.getId());
        }
    }

    public BloomFilter<Long> getGoodsBloomFilter(){
        return goodsBloomFilter;
    }
}
