package com.ltri.seckill.constant;

/**
 * @author ltri
 * @date 2020/4/18 11:02 上午
 */
public class Constant {
    public static final Integer OK = 200;

    public static final Integer SYSTEM_ERROR = 500;

    public static final String GOODS_STOCK_PRE = "goods:stock:long:";

    public static final String GOODS_LOCK_PRE = "goods:lock:string:";

    public static final String RECEIVE_LOG = "receive.log:info:string";

    public static final String RECEIVE_LOCK = "receive.log:lock:string";

    public static final String RECEIVE_TX_KEY_PRE = "receive.log:key:string:";

    public static final String SEND_TX_KEY_PRE = "send.log:key:string:";

    public static final String SECKILL_GOODS_USER_PRE = "goods:seckill.user:long:";

    public static final String SECKILL_GOODS_USER_LOCK_PRE = "goods:seckill.user.lock:long:";

    public static final String TOPIC_SECKILL_ORDERS = "topic-seckill-orders";

    public static final String TOPIC_ORDERS_PAY = "topic-orders-pay";

    public static final String TOKEN_PRE = "token:string:";

    public static final String IDEMPOTENT = "idempotent";

    public static final String IDEMPOTENT_LOCK = "idempotent:lock";
}
