package com.ltri.seckill.biz;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.hash.BloomFilter;
import com.ltri.seckill.config.SeckillGoodsInit;
import com.ltri.seckill.constant.Constant;
import com.ltri.seckill.dto.SeckillDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * @author ltri
 * @date 2020/4/18 10:58 上午
 */
@Service
@Slf4j
public class SeckillBiz {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private SeckillGoodsInit seckillGoodsInit;

    public void seckill(Long goodsId, Long userId) {
        //秒杀商品Id验证
        if (!checkGoodsId(goodsId)) {
            return;
        }
        // 判断当前用户是否已参与秒杀
        if (!addSeckillUser(goodsId, userId)) {
            return;
        }
        //库存减少
        decRedisStock(goodsId, userId);
    }

    /**
     * 验证秒杀商品Id，是否合法；是否还有库存
     */
    private boolean checkGoodsId(Long goodsId) {
        //布隆过滤器过滤非法id
        BloomFilter<Long> goodsBloomFilter = seckillGoodsInit.getGoodsBloomFilter();
        if (!goodsBloomFilter.mightContain(goodsId)) {
            log.error("bloom filter id不合法 id:{}", goodsId);
            return false;
        }
        //二次缓存确认
        String res = stringRedisTemplate.opsForValue().get(Constant.GOODS_STOCK_PRE + goodsId);
        if (res == null) {
            log.error("id不合法 id:{}", goodsId);
            return false;
        }
        if (Long.parseLong(res) <= 0) {
            log.info("秒杀商品id {}秒杀结束", goodsId);
            return false;
        }
        return true;
    }

    /**
     * 判断当前用户是否已参与秒杀,没有则添加到redis
     */
    private boolean addSeckillUser(Long goodsId, Long userId) {
        if (BooleanUtils.isTrue(stringRedisTemplate.opsForSet().isMember(Constant.SECKILL_GOODS_USER_PRE + goodsId, userId.toString()))) {
            log.info("当前用户{}秒杀商品{}已存在", userId, goodsId);
            return false;
        }
        String lockKey = Constant.SECKILL_GOODS_USER_LOCK_PRE + goodsId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            //加锁
            lock.lock();
            log.info("加锁查询已参与秒杀用户列表key:{}", lockKey);
            //二次判断用户是否已参与秒杀
            Boolean userIsExist = stringRedisTemplate.opsForSet().isMember(Constant.SECKILL_GOODS_USER_PRE + goodsId, userId.toString());
            if (BooleanUtils.isTrue(userIsExist)) {
                log.info("当前用户{}秒杀商品{}已存在", userId, goodsId);
                return false;
            }
            //添加秒杀用户
            stringRedisTemplate.opsForSet().add(Constant.SECKILL_GOODS_USER_PRE + goodsId, userId.toString());
            return true;
        } finally {
            log.info("解锁已参与秒杀用户列表key:{}", lockKey);
            lock.unlock();
        }
    }

    /**
     * redis库存减少 消息队列消息发送
     */
    private void decRedisStock(Long goodsId, Long userId) {
        //分布式锁查询
        String lockKey = Constant.GOODS_LOCK_PRE + goodsId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            //加锁
            lock.lock();
            log.info("加锁查询库存key:{}", lockKey);
            String redisStock = stringRedisTemplate.opsForValue().get(Constant.GOODS_STOCK_PRE + goodsId);
            if (redisStock == null || Long.parseLong(redisStock) <= 0) {
                log.info("秒杀结束");
                return;
            }
            //缓存库存减少
            stringRedisTemplate.opsForValue().decrement(Constant.GOODS_STOCK_PRE + goodsId);

            //发送消息
            SeckillDTO seckillDTO = new SeckillDTO();
            seckillDTO.setGoodsId(goodsId);
            seckillDTO.setUserId(userId);
            seckillDTO.setTxKey(IdWorker.get32UUID());
            log.info("1.发送消息{}", JSON.toJSONString(seckillDTO));
            Message<SeckillDTO> message = MessageBuilder.withPayload(seckillDTO).build();
            rocketMQTemplate.asyncSend(Constant.TOPIC_SECKILL_ORDERS, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("秒杀异步消息成功 sendResult{}", sendResult);
                }

                @Override
                public void onException(Throwable throwable) {
                    log.info("秒杀异步消息失败 {}", throwable.getMessage());
                }
            });
        } finally {
            log.info("解锁 key:{}", lockKey);
            lock.unlock();
        }
    }
}
