# NettyRpc
A rpc project based on netty,zookeeper and springboot.

**更多详情请参考个人博客 ：https://mxecy.cn/post/spring-netty-rpc/**


一个基于netty,zookeeper,springboot的练手rpc项目。由于是练手项目，所以可能会有一些问题，欢迎指出。
[文档参考](https://github.com/Gloduck/NettyRpc/blob/main/Rpc/%E6%96%87%E6%A1%A3.md)

# Todo List

+ 做一个Starter。
+ 优化`RpcProxyBeanDefinitionRegistryPostProcessor`，从指定的包下扫描，而非所有的包下扫描。
+ 添加更多的序列化器。
+ 服务提供者非正常关闭然后立即重启，对临时节点的处理优点问题。
+ 只根据消息头和长度判断消息有问题，还需要加一个字符表示结尾。否则如果传输图片之类的会报错，如果图片头部和魔数一样，可能会导致Netty解析消息出错。


# Usage

## 客户端

+ 配置注册中心

  ```java
  @Bean
  public Registry registry(){
      RegistryConfig config = RegistryConfig.builder()
          .connectionTimeout(500)
          .timeout(500)
          .ephemeral(true)
          .address("127.0.0.1:2181").build();
      Registry registry = new ZookeeperRegistry(config);
      return registry;
  }
  ```

+ 配置Netty客户端

  ```java
  @Bean
  public NettyClient nettyClient(){
      NettyConfig config = NettyConfig.clientBuilder()
          .port(8027)
          .serializer(FastJsonSerializer.class)
          .requestTimeout(5000)
          .build();
      NettyClient nettyClient = new NettyClient(config);
      return nettyClient;
  }
  ```

+ 使用`@RpcClient`注解RPC方法的调用接口，并使用`@RpcReference`指定服务信息。会为注解了`@RpcClient`的接口生成代理实现类，并注入到Spring容器中。对于需要异步调用的方法，将返回值设置为`ResponseFuture`

  ```java
  @RpcClient
  public interface TestService {
  
      @RpcReference(serviceName = "getUser",loadBlance = LoadBlance.IP_HASH)
      User getUser(Integer id, String name, String password);
  
      @RpcReference(serviceName = "updateUser", loadBlance = LoadBlance.RANDOM)
      int updateUser(User user);
  
      @RpcReference(serviceName = "async",loadBlance = LoadBlance.RANDOM)
      ResponseFuture<String> async();
  }
  
  ```

## 服务端

+ 配置注册中心

  ```java
  @Bean
  public Registry registry(){
      RegistryConfig config = RegistryConfig.builder()
          .connectionTimeout(500)
          .timeout(500)
          .ephemeral(true)
          .address("127.0.0.1:2181").build();
      Registry registry = new ZookeeperRegistry(config);
      return registry;
  }
  ```

+ 配置Netty服务端

  ```java
  @Bean
  public NettyServer nettyServer(){
      NettyConfig config = NettyConfig.serverBuilder()
          .address(NetUtil.getLocalHost(), 8026)
          .serializer(FastJsonSerializer.class)
          .heartBeatTimes(3)
          .heartBeatInterval(5)
          .build();
      NettyServer nettyServer = new NettyServer(config);
      return nettyServer;
  }
  ```

+ 使用`@RpcService`注解需要被注册的服务，权重默认是当前CPU的核心数

  ```java
  @Service
  public class TestService {
      @RpcService(serviceName = "updateUser")
      public int updateUser(User user){
          System.out.println(user.toString());
          return 1;
      }
      @RpcService(serviceName = "getUser",weight = 5)
      public User getUser(Integer id, String name, String password){
          User user = new User();
          user.setId(id);
          user.setName(name);
          user.setPassword(password);
          return user;
      }
  
      @RpcService(serviceName = "async")
      public String async(){
          try {
              Thread.sleep(1000);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          return "Hello";
      }
  }
  
  ```

  
