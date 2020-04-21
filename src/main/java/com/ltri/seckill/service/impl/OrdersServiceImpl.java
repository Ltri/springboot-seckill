package com.ltri.seckill.service.impl;

import com.ltri.seckill.entity.Orders;
import com.ltri.seckill.mapper.OrdersMapper;
import com.ltri.seckill.service.IOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ltri
 * @since 2020-04-18
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

}
