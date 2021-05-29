package cn.gloduck.test;

import cn.gloduck.netty.rpc.ref.client.Instance;
import cn.gloduck.netty.rpc.ref.client.ServiceInstance;
import cn.gloduck.netty.rpc.registry.Registry;
import cn.gloduck.netty.rpc.registry.RegistryConfig;
import cn.gloduck.netty.rpc.registry.zk.ZookeeperRegistry;

import java.util.List;

public class BaseTest {
    public static void main(String[] args) {
        RegistryConfig config = RegistryConfig.builder()
                .address("127.0.0.1:2181")
                .timeout(500)
                .connectionTimeout(500)
                .ephemeral(false)
                .build();
        Registry registry = new ZookeeperRegistry(config);
        registry.init();
        registry.start();
        registry.registrySingle("127.0.0.1", 8080, "show");
        registry.registrySingle("127.0.0.1", 8080, "start");
        registry.registrySingle("127.0.0.1", 8080, "stop");
        registry.registrySingle("127.0.0.1", 8080, "test");
        registry.registrySingle("127.0.0.2", 8080, "show");


        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                ServiceInstance show = registry.discover("show");
            }).start();
        }

        ServiceInstance show = registry.discover("show");
        System.out.println(show);
    }
}
