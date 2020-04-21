# Spring Boot简易秒杀服务
## 流程图
![流程图](https://github.com/Ltri/springboot-seckill/blob/master/seckill.jpg)
## 常见问题解决方案
### 1.超卖问题解决
- 采用redisson实现分布式锁方式解决超卖问题
### 2.限流解决方案
- 可接入jwt进行用户的鉴权
- BloomFilter布隆过滤器解决缓存穿透问题
- 分布式锁解决缓存击穿问题
### 3.重复下单
- 参加秒杀后用户通过分布式锁方式加入redis缓存
- 添加流水表
### 4.分布式事务
- 采用rocketMQ事务消息实现事务最终一致性

## 后续可优化
### 1.接口URL动态化
- 接口加密
### 2.库存预热
- 可通过定时任务方式吧库存和BloomFilter加载
### 3.中间件集群化
- 搭建redis高可用集群
- 搭建rocketMQ高可用集群
- MySQL读写分离
