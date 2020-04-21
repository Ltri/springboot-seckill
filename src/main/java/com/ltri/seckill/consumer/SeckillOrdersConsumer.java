package com.ltri.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ltri.seckill.constant.Constant;
import com.ltri.seckill.dto.SeckillDTO;
import com.ltri.seckill.entity.Orders;
import com.ltri.seckill.entity.SendLog;
import com.ltri.seckill.service.IGoodsService;
import com.ltri.seckill.service.IOrdersService;
import com.ltri.seckill.service.ISendLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @author ltri
 * @date 2020/4/16 9:30 下午
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "topic-seckill-orders", consumerGroup = "group-seckill-orders")
public class SeckillOrdersConsumer implements RocketMQListener<SeckillDTO> {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IOrdersService ordersService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ISendLogService sendLogService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(SeckillDTO seckillDTO) {
        log.info("rocketMq入参{}", JSON.toJSONString(seckillDTO));
        String type = "order:pay:send";
        String key = Constant.SEND_TX_KEY_PRE + seckillDTO.getTxKey() + type;
        if (BooleanUtils.isTrue(stringRedisTemplate.hasKey(key))) {
            log.info("重复消息{}", JSON.toJSON(seckillDTO));
            return;
        }
        //消息幂等处理
        int count = sendLogService.count(Wrappers.<SendLog>lambdaQuery().eq(SendLog::getTxKey, seckillDTO.getTxKey()).eq(SendLog::getType, type));
        if (count > 0) {
            log.info("重复消息{}", JSON.toJSON(seckillDTO));
            //加入缓存
            stringRedisTemplate.opsForValue().set(key, seckillDTO.getGoodsId().toString(), 30, TimeUnit.SECONDS);
            return;
        }
        //库存减少
        goodsService.decStock(seckillDTO.getGoodsId());
        Orders orders = new Orders();
        orders.setGoodsId(seckillDTO.getGoodsId());
        orders.setUserId(seckillDTO.getUserId());
        orders.setStatus(1);
        //订单生成
        ordersService.save(orders);
        //加入缓存
        stringRedisTemplate.opsForValue().set(key, seckillDTO.getGoodsId().toString(), 30, TimeUnit.SECONDS);
    }
}
