package com.ltri.seckill.service;

import com.ltri.seckill.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ltri
 * @since 2020-04-18
 */
public interface IGoodsService extends IService<Goods> {

    void decStock(Long id);

}
