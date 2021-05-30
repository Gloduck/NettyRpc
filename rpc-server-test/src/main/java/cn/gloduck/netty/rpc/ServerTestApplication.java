package cn.gloduck.netty.rpc;

import cn.gloduck.netty.rpc.registry.Registry;
import cn.gloduck.netty.rpc.registry.RegistryConfig;
import cn.gloduck.netty.rpc.registry.zk.ZookeeperRegistry;
import cn.gloduck.netty.rpc.serializer.jdk.JdKSerializer;
import cn.gloduck.netty.rpc.serializer.json.FastJsonSerializer;
import cn.gloduck.netty.rpc.transport.NettyConfig;
import cn.gloduck.netty.rpc.transport.server.NettyServer;
import cn.gloduck.netty.rpc.utils.NetUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServerTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerTestApplication.class);
    }


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
}
