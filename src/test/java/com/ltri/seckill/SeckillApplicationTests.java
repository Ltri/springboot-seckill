package com.ltri.seckill;

import com.ltri.seckill.biz.OrdersBiz;
import com.ltri.seckill.constant.Constant;
import com.ltri.seckill.dto.PayDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class SeckillApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private OrdersBiz ordersBiz;

    @Test
    void contextLoads() {
        PayDTO payDTO = new PayDTO();
        payDTO.setTxKey("aaaaa");
        payDTO.setOrdersId(1251892917228441601L);
        ordersBiz.ordersPayCallback(payDTO);
    }

    @Test
    void test() {
        System.out.println(stringRedisTemplate.opsForSet().isMember(Constant.SECKILL_GOODS_USER_PRE + 1, "222"));
    }

}
