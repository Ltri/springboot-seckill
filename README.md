# Spring Boot简易秒杀服务
## 流程图
![流程图](https://github.com/Ltri/springboot-seckill/blob/master/seckill.jpg)
## 常见问题解决方案
### 1.超卖问题解决
- 采用redisson实现分布式锁方式解决超卖问题
### 2.限流解决方案
- 可接入jwt进行用户的鉴权
- BloomFilter布隆过滤器解决缓存穿透问题
- redis对已秒杀用户
- 分布式锁解决缓存击穿问题
### 3.分布式事务
- 采用rocketMQ事务消息实现事务最终一致性
