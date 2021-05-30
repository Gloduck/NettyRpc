package cn.gloduck.netty.rpc.scan;

import cn.gloduck.netty.rpc.annotation.RpcService;
import cn.gloduck.netty.rpc.listener.NettyStateListener;
import cn.gloduck.netty.rpc.listener.impl.NettyRegistryListener;
import cn.gloduck.netty.rpc.registry.Registry;
import cn.gloduck.netty.rpc.transport.server.NettyServer;
import cn.gloduck.netty.rpc.utils.NetUtil;
import cn.gloduck.netty.rpc.utils.RuntimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * RPC服务启动引导
 * @author Gloduck
 */
@Component
public class RpcServerBootstrap implements ApplicationListener<WebServerInitializedEvent>, ApplicationContextAware, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerBootstrap.class);
    private final Registry registry;
    private final NettyServer nettyServer;
    private ApplicationContext context;

    public RpcServerBootstrap(Registry registry, NettyServer nettyServer) {
        this.registry = registry;
        this.nettyServer = nettyServer;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        // 初始化注册中心
        logger.info("初始化注册中心");
        registry.init();
        // 启动注册中心
        logger.info("启动注册中心");
        registry.start();
        // 启动Netty服务器
        logger.info("启动NettyRPC服务器");
        nettyServer.start();
        // 创建Netty事件监听器，需要在netty启动之后，防止重复注册
        NettyStateListener listener = new NettyRegistryListener(registry);
        nettyServer.addListener(listener);
        logger.info("开始扫描RPC服务");
        doScan();
    }

    public void doScan(){
        String host = NetUtil.getLocalHost();
        int port = nettyServer.getPort();
        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = context.getBean(beanDefinitionName);
            Class<?> beanClass = bean.getClass();
            Method[] methods = beanClass.getMethods();
            for (Method method : methods) {
                RpcService annotation = method.getAnnotation(RpcService.class);
                if(annotation != null){
                    String serviceName = annotation.serviceName();
                    int weight = annotation.weight();
                    weight = weight <= 0 ? RuntimeUtil.availableProcessors() : weight;
                    // 注册服务到注册中心
                    registry.registrySingle(host, port, serviceName, weight);
                    // 注册Bean，方法，服务名的对应
                    nettyServer.registryServiceBean(serviceName, bean, method);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        logger.info("关闭Netty服务器");
        nettyServer.stop();
        logger.info("关闭注册中心");
        registry.stop();
    }
}
