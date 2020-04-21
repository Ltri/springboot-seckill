package com.ltri.seckill.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author ltri
 * @date 2020/4/18 4:53 下午
 */
@Data
public class SeckillDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long goodsId;

    private String txKey;

}
