package cn.gloduck.netty.rpc.scan;

import cn.gloduck.netty.rpc.registry.Registry;
import cn.gloduck.netty.rpc.transport.client.ConnectionManager;
import cn.gloduck.netty.rpc.transport.client.NettyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * rpc客户端引导
 * @author Gloduck
 */
@Component
public class RpcClientBootstrap  implements ApplicationListener<WebServerInitializedEvent>, DisposableBean{
    private final static Logger logger = LoggerFactory.getLogger(RpcClientBootstrap.class);
    private Registry registry;
    private NettyClient client;

    public RpcClientBootstrap(Registry registry, NettyClient client) {
        this.registry = registry;
        this.client = client;
    }

    @Override
    public void destroy() throws Exception {

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
        logger.info("启动NettyRPC客户端");
        client.start();
        logger.info("初始化ConnectionManager");
        ConnectionManager.instance().setRegistry(registry);
        ConnectionManager.instance().setTransporterCreator(client);
    }
}
