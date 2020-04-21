package com.ltri.seckill.controller;

import com.ltri.seckill.biz.OrdersBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ltri
 * @date 2020/4/19 11:22 下午
 */
@RestController
public class OrdersController {
    @Autowired
    private OrdersBiz ordersBiz;

    @PostMapping("/orders/pay/{ordersId}")
    public void pay(@PathVariable Long ordersId) {
        ordersBiz.pay(ordersId);
    }
}
