package com.ltri.seckill.biz;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ltri.seckill.constant.Constant;
import com.ltri.seckill.dto.PayDTO;
import com.ltri.seckill.entity.Orders;
import com.ltri.seckill.entity.SendLog;
import com.ltri.seckill.service.IOrdersService;
import com.ltri.seckill.service.ISendLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @author ltri
 * @date 2020/4/19 11:22 下午
 */
@Service
@Slf4j
public class OrdersBiz {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private ISendLogService sendLogService;
    @Autowired
    private IOrdersService ordersService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void pay(Long ordersId) {
        //调用第三方支付接口
        aliPay();

        //事务消息发送
        PayDTO payDTO = new PayDTO();
        payDTO.setOrdersId(ordersId);
        //流水唯一码
        payDTO.setTxKey(IdWorker.get32UUID());
        Message<PayDTO> message = MessageBuilder.withPayload(payDTO).build();
        log.info("1.发送事务消息{}", JSON.toJSONString(payDTO));
        rocketMQTemplate.sendMessageInTransaction("topic-orders-pay", message, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void ordersPayCallback(PayDTO payDTO) {
        log.info("4.回调消息开始处理{}", JSON.toJSONString(payDTO));
        String key = Constant.SEND_TX_KEY_PRE + payDTO.getTxKey();
        if (BooleanUtils.isTrue(stringRedisTemplate.hasKey(key))) {
            log.info("重复消息{}", JSON.toJSON(payDTO));
            return;
        }
        //消息幂等处理
        int count = sendLogService.count(Wrappers.<SendLog>lambdaQuery().eq(SendLog::getTxKey, payDTO.getTxKey()));
        if (count > 0) {
            log.info("重复消息{}", JSON.toJSON(payDTO));
            //加入缓存
            stringRedisTemplate.opsForValue().set(key, payDTO.getOrdersId().toString(), 30, TimeUnit.SECONDS);
            return;
        }
        //订单状态更新
        Orders orders = ordersService.getById(payDTO.getOrdersId());
        orders.setStatus(2);
        ordersService.updateById(orders);

        //流水表插入
        SendLog sendLog = new SendLog();
        sendLog.setType("orders:pay");
        sendLog.setTxKey(payDTO.getTxKey());
        sendLogService.save(sendLog);
        //加入缓存
        stringRedisTemplate.opsForValue().set(key, payDTO.getOrdersId().toString(), 30, TimeUnit.SECONDS);
        log.info("5.流水表插入{}", JSON.toJSONString(sendLog));
        log.info("6.回调消息处理结束");
    }


    /**
     * 模拟调用第三方接口
     */
    private void aliPay() {
        log.info("支付成功");
    }
}
