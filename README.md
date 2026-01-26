一个基于 Netty 和 ZooKeeper 的高性能 Java RPC 框架，支持服务注册发现、负载均衡、容错机制（限流、重试、熔断）、链路追踪与心跳检测。

## 🚀 特性

- **服务注册与发现**: 基于 ZooKeeper 实现，支持动态服务发现和本地缓存
- **负载均衡**: 支持轮询（Round Robin）和随机（Random）策略
- **序列化**: 支持 JSON（FastJSON2）、Java 原生序列化和 Protostuff
- **链路追踪**: 内置 TraceContext/TraceId，支持 Zipkin 上报
- **心跳检测**: 客户端/服务端空闲检测，自动发送与处理心跳包
- **容错机制**:
  - **限流**: 令牌桶算法实现
  - **重试**: 基于 Guava Retry，支持可配置的重试策略
  - **熔断**: 三态熔断器（CLOSED/OPEN/HALF_OPEN）
- **灵活配置**: 基于 YAML 配置文件，无需修改代码
- **高性能传输**: 基于 Netty NIO 框架

## 📦 项目结构

```
J-RPC
├── rpc-api           # 服务接口定义
├── rpc-common        # 公共组件（实体类、常量、枚举）
├── rpc-core          # 核心框架实现
│   ├── config        # 配置管理
│   ├── transport     # 网络传输（Netty）
│   ├── serializers   # 序列化器
│   ├── registry      # 服务注册与发现
│   ├── loadbalance   # 负载均衡
│   ├── fault         # 容错机制
│   ├── proxy         # 客户端代理
│   └── provider      # 服务提供者管理
├── rpc-provider      # 服务提供者示例
└── rpc-consumer      # 服务消费者示例
```

## 🛠️ 技术栈

- **JDK**: 19+
- **Netty**: 4.1.51.Final - 高性能网络传输框架
- **ZooKeeper**: 服务注册中心（通过 Curator 5.1.0 操作）
- **FastJSON2**: 2.0.57 - JSON 序列化
- **Protostuff**: 1.7.4 - 高性能二进制序列化
- **Zipkin**: 3.4.0 - 链路追踪数据上报
- **Guava Retry**: 重试机制
- **Lombok**: 简化代码
- **Log4j2**: 日志框架
- **SnakeYAML**: YAML 配置解析

## 🚀 快速开始

### 1. 环境准备

- JDK 19+
- Maven 3.6+
- ZooKeeper 服务

### 2. 配置 ZooKeeper

修改配置文件中的 ZooKeeper 地址：

**rpc-provider/src/main/resources/application.yml**
```yaml
zookeeper:
  host: 你的ZooKeeper地址
  port: 2181
```

**rpc-consumer/src/main/resources/application.yml**
```yaml
zookeeper:
  host: 你的ZooKeeper地址
  port: 2181
```

### 3. 构建项目

```bash
mvn clean install
```

### 4. 启动服务提供者

```bash
cd rpc-provider
mvn exec:java -Dexec.mainClass="com.tgu.provider.TestProvider"
```

### 5. 启动服务消费者

```bash
cd rpc-consumer
mvn exec:java -Dexec.mainClass="com.tgu.consumer.TestConsumer"
```

## 📖 配置说明

### Provider 配置示例

```yaml
# ZooKeeper配置
zookeeper:
  host: 172.31.151.142
  port: 2181
  root-path: RPC
  retry-path: CanRetry
  session-timeout: 40000
  connection-timeout: 15000

# Zipkin 配置
zipkin:
  url: http://localhost:9411/api/v2/spans

# 心跳配置
heartbeat:
  server:
    reader-idle-time: 10
    writer-idle-time: 20

# 服务端配置
server:
  host: 127.0.0.1
  port: 9999

# 序列化配置
serializer:
  type: json  # json / object / protostuff

# 限流配置
fault-tolerance:
  rate-limit:
    enabled: true
    rate: 200      # 令牌生成速率(ms)
    capacity: 100  # 令牌桶容量
```

### Consumer 配置示例

```yaml
# ZooKeeper配置
zookeeper:
  host: 172.31.151.142
  port: 2181
  root-path: RPC
  retry-path: CanRetry
  session-timeout: 40000
  connection-timeout: 15000

# Zipkin 配置
zipkin:
  url: http://localhost:9411/api/v2/spans

# 心跳配置
heartbeat:
  client:
    writer-idle-time: 5

# 客户端配置
client:
  load-balance: round  # round(轮询) 或 random(随机)

# 序列化配置
serializer:
  type: json  # json / object / protostuff

# 重试配置
fault-tolerance:
  retry:
    enabled: true
    max-attempts: 3
    wait-time: 700
    wait-time-unit: MILLISECONDS

# 熔断器配置
  circuit-breaker:
    enabled: true
    failure-threshold: 2
    half-open-success-rate: 0.4
    rest-time-period: 3000
```

## 🎯 核心流程

### 服务注册

1. Provider 启动时将服务注册到 ZooKeeper
2. 在 ZooKeeper 中创建临时节点（格式：`/RPC/接口名/IP:端口`）
3. Provider 关闭时，临时节点自动删除

### 服务调用

1. Consumer 通过动态代理拦截方法调用
2. 从 ZooKeeper 获取服务地址列表
3. 负载均衡选择目标服务器
4. 熔断器判断是否允许请求
5. 序列化请求并通过 Netty 发送
6. Provider 接收请求，限流检查后处理
7. 反序列化响应并返回结果
8. 失败时根据配置进行重试

## 🛡️ 容错机制

### 限流（Provider 端）

采用令牌桶算法：
- 配置令牌生成速率和桶容量
- 请求获取令牌成功才能处理
- 保护服务端不被过载

### 重试（Consumer 端）

基于 Guava Retry：
- 可配置最大重试次数
- 可配置重试间隔时间
- 支持异常重试和失败结果重试

### 熔断（Consumer 端）

三态熔断器：
- **CLOSED（关闭）**: 正常处理请求，失败次数达阈值后打开
- **OPEN（打开）**: 拒绝所有请求，等待恢复时间
- **HALF_OPEN（半开）**: 允许部分请求，成功率达标后关闭

## 📊 测试场景

`TestConsumer` 包含完整的熔断器测试场景：

1. **第一批**: 6个并发请求，触发限流和熔断
2. **第二批**: 验证熔断器 OPEN 状态
3. **第三批**: 等待后测试 HALF_OPEN 状态
4. **第四批**: 验证熔断器恢复 CLOSED 状态

## 🔧 自定义服务

### 1. 定义服务接口（rpc-api）

```java
public interface YourService {
    YourResult yourMethod(YourParam param);
}
```

### 2. 实现服务（rpc-provider）

```java
public class YourServiceImpl implements YourService {
    @Override
    public YourResult yourMethod(YourParam param) {
        // 实现逻辑
        return result;
    }
}
```

### 3. 注册服务

```java
YourService service = new YourServiceImpl();
serviceProvider.provideServiceInterface(service, false);
```

### 4. 调用服务（rpc-consumer）

```java
ClientProxy clientProxy = new ClientProxy();
YourService proxy = clientProxy.getProxy(YourService.class);
YourResult result = proxy.yourMethod(param);
```

## 📝 更新日志

### v1.2 (当前版本)
- ✨ 新增链路追踪（TraceContext/TraceId）与 Zipkin 上报
- ✨ 新增客户端/服务端心跳检测与空闲处理
- ✨ 新增 Protostuff 序列化
- 🔧 优化 Netty 处理链路与编码解码流程
- 📖 更新配置示例与说明

### v1.1
- ✨ 支持 YAML 配置文件
- ✨ 新增配置验证工具
- ✨ 支持动态配置负载均衡策略
- ✨ 支持动态配置序列化方式
- 🔧 优化配置管理，废弃硬编码配置
- 📖 完善文档和快速开始指南

### v1.0
- 🎉 初始版本发布
- ✅ 支持基本 RPC 调用
- ✅ 支持服务注册与发现
- ✅ 支持负载均衡
- ✅ 支持限流、重试、熔断

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 MIT 许可证。

## 👥 作者

- kouxh02

## 🔗 相关链接

- [GitHub 仓库](https://github.com/kouxh02/J-RPC)