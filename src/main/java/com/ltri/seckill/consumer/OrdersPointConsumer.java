package com.ltri.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ltri.seckill.constant.Constant;
import com.ltri.seckill.dto.PayDTO;
import com.ltri.seckill.entity.ReceiveLog;
import com.ltri.seckill.service.IReceiveLogService;
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
 * @date 2020/4/16 11:42 下午
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "topic-orders-pay", consumerGroup = "group-orders-point")
public class OrdersPointConsumer implements RocketMQListener<PayDTO> {
    @Autowired
    private IReceiveLogService receiveLogService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(PayDTO dto) {
        log.info("7.积分消费者消费消息开始{}", JSON.toJSONString(dto));
        String type = "order:pay:point";
        String key = Constant.RECEIVE_TX_KEY_PRE + dto.getTxKey() + type;
        if (BooleanUtils.isTrue(stringRedisTemplate.hasKey(key))) {
            log.info("积分消费者重复消息{}", JSON.toJSON(dto));
            return;
        }
        //消息幂等处理
        int count = receiveLogService.count(Wrappers.<ReceiveLog>lambdaQuery().eq(ReceiveLog::getTxKey, dto.getTxKey()).eq(ReceiveLog::getType, type));
        if (count > 0) {
            log.info("积分消费者重复消息{}", JSON.toJSON(dto));
            //加入缓存
            stringRedisTemplate.opsForValue().set(key, dto.getOrdersId().toString(), 30, TimeUnit.SECONDS);
            return;
        }
        System.out.println("模拟积分服务");
        log.info("模拟积分服务 订单id:{}", dto.getOrdersId());
        //接收流水表插入
        ReceiveLog receiveLog = new ReceiveLog();
        receiveLog.setTxKey(dto.getTxKey());
        receiveLog.setType(type);
        receiveLogService.save(receiveLog);
        stringRedisTemplate.opsForValue().set(key, dto.getOrdersId().toString(), 30, TimeUnit.SECONDS);
        log.info("8.消费者消费消息结束");
    }
}
