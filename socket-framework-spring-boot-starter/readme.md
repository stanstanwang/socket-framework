版本升级说明：

### 1.1.4-SNAPSHOT 不兼容的升级

1. NettyClientManager 不在spring容器里边了， 需要使用新的连接，通过 NettyClientBuilder 来创建新的连接

```java

// 这个是之前的方式
Channel mainChannel=nettyClientManger.getChannel();

// 这个是新的方式
Channel mainChannel=clientBuilder.build().getChannel();

```

2. ChannelLifecycle 更名为 ServerConnectionListener, 客户端的建立连接的监听请使用 ConnectionListener 
