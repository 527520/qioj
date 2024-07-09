## 将 backend 分支的单体项目改造为了微服务
- 选用Spring Cloud Alibaba重构单体项目
- 将模块划分为用户服务、题目服务、判题服务、公共模块（commons、gateway、model、service-client）
- 使用Redis分布式Session存储用户登录信息
## Nacos + OpenFeign 实现各模块间的相互调用
## 使用Spring Cloud Gateway对各服务接口进行聚合和路由
## 自定义CorsWebFilter Bean解决了全局跨域
## 使用Knife4j Gateway实现了聚合文档
## 使用RabbitMQ消息队列异步提交判题
