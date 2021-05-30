package cn.gloduck.netty.rpc;

import cn.gloduck.netty.rpc.entity.User;
import cn.gloduck.netty.rpc.registry.Registry;
import cn.gloduck.netty.rpc.registry.RegistryConfig;
import cn.gloduck.netty.rpc.registry.zk.ZookeeperRegistry;
import cn.gloduck.netty.rpc.serializer.jdk.JdKSerializer;
import cn.gloduck.netty.rpc.serializer.json.FastJsonSerializer;
import cn.gloduck.netty.rpc.service.TestService;
import cn.gloduck.netty.rpc.transport.NettyConfig;
import cn.gloduck.netty.rpc.transport.client.ConnectionManager;
import cn.gloduck.netty.rpc.transport.client.NettyClient;
import cn.gloduck.netty.rpc.transport.client.ResponseFuture;
import cn.gloduck.netty.rpc.utils.NetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class ClientTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientTestApplication.class);
    }

    @RestController
    public static class TestController{
        @Autowired
        TestService testService;
        @GetMapping("user")
        public String user(){

            User user = testService.getUser(1, "gloduck", "gloduck");
            return user.toString();
        }

        @GetMapping("update")
        public String update(){
            int update = testService.updateUser(new User(1,"gloduck","gloduck"));
            return Integer.toString(update);
        }

        @GetMapping("async")
        public String asyncTest(){
            String msg = "before_";
            ResponseFuture<String> async = testService.async();
            try {
                String value = async.get();
                msg += value;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return msg;
        }
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
    public NettyClient nettyClient(){
        NettyConfig config = NettyConfig.clientBuilder()
                .port(8027)
//                .serializer(FastJsonSerializer.class)
                .requestTimeout(5000)
                .build();
        NettyClient nettyClient = new NettyClient(config);
        return nettyClient;
    }
}
