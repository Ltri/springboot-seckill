package com.ltri.seckill.controller;

import com.ltri.seckill.biz.SeckillBiz;
import com.ltri.seckill.dto.SeckillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author ltri
 * @date 2020/4/18 10:59 上午
 */
@RestController
public class SeckillController {
    @Autowired
    private SeckillBiz seckillBiz;

    @PostMapping("/seckill")
    public void seckill(@RequestBody @Valid SeckillDTO seckillDTO) {
        seckillBiz.seckill(seckillDTO.getGoodsId(), seckillDTO.getUserId());
    }
}
