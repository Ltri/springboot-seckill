package com.ltri.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ltri.seckill.biz.OrdersBiz;
import com.ltri.seckill.dto.PayDTO;
import com.ltri.seckill.entity.SendLog;
import com.ltri.seckill.service.ISendLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @author ltri
 * @date 2020/4/19 11:37 下午
 */
@Component
@RocketMQTransactionListener
@Slf4j
public class OrdersPayListener implements RocketMQLocalTransactionListener {
    @Autowired
    private OrdersBiz ordersBiz;
    @Autowired
    private ISendLogService sendLogService;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        try {
            String msg = new String((byte[]) message.getPayload());
            log.info("2.解析到消息{}", msg);
            PayDTO payDTO = JSON.parseObject(msg, PayDTO.class);
            log.info("3.事务回调{}", payDTO);
            ordersBiz.ordersPayCallback(payDTO);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 事务状态回查
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        log.info("事务状态回查");
        String msg = new String((byte[]) message.getPayload());
        log.info("2.解析到消息{}", msg);
        PayDTO payDTO = JSON.parseObject(msg, PayDTO.class);
        int count = sendLogService.count(Wrappers.<SendLog>lambdaQuery().eq(SendLog::getTxKey, payDTO.getTxKey()));
        //数据已发送
        if (count > 0) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
