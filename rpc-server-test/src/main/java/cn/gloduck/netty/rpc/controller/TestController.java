package cn.gloduck.netty.rpc.controller;

import cn.gloduck.netty.rpc.ref.client.ServiceInstance;
import cn.gloduck.netty.rpc.ref.server.BeanAndMethod;
import cn.gloduck.netty.rpc.ref.server.ServiceInfo;
import cn.gloduck.netty.rpc.registry.Registry;
import cn.gloduck.netty.rpc.transport.server.NettyServer;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.gloduck.netty.rpc.service.TestService;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class TestController {
    @Autowired
    private TestService testService;
    @Autowired
    private Registry registry;
    @Autowired
    private NettyServer nettyServer;

    @GetMapping("/startNetty")
    public String startNetty(){
        nettyServer.start();
        return "ok";
    }

    @GetMapping("/stopNetty")
    public String stopNetty(){
        nettyServer.stop();
        return "ok";
    }

}
