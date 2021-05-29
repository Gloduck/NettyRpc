package cn.gloduck.netty.rpc.transport.server;

import cn.gloduck.netty.rpc.ref.server.BeanAndMethod;
import cn.gloduck.netty.rpc.transport.AbstractNettyServer;
import cn.gloduck.netty.rpc.transport.NettyConfig;
import cn.gloduck.netty.rpc.transport.functional.BeanMethodRegistration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * netty服务器
 * @author Gloduck
 */
public  class NettyServer extends AbstractNettyServer implements BeanMethodRegistration {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private Map<String, BeanAndMethod> serviceBeanMapping;
/*    private EventLoopGroup boss;
    private EventLoopGroup worker;*/
//    private Thread thread;
    private ServerBootstrap bootstrap;

    public NettyServer(NettyConfig nettyConfig) {
        super(nettyConfig);
        this.serviceBeanMapping = new HashMap<>(32);
    }


    @Override
    public int getPort() {
        return nettyConfig.getPort();
    }

    /**
     * 注册服务名关联的bean
     * @param serviceName
     * @param bean
     */
    @Override
    public void registryServiceBean(String serviceName, Object bean, Method method){
        if(bean == null || method == null){
            logger.warn("Bean or Method is null , registry service : {} failed", serviceName);
            return;
        }
        this.serviceBeanMapping.put(serviceName, new BeanAndMethod(bean, method));
    }

    @Override
    public void unRegistryServiceBean(String serviceName) {
        this.serviceBeanMapping.remove(serviceName);
    }
    @Override
    protected boolean initNettyServer() throws Exception{
        if(bootstrap != null){
            return true;
        }
        synchronized (this){
            if(bootstrap != null){
                return true;
            }
            boolean flag = false;
            EventLoopGroup boss = new NioEventLoopGroup(1);
            EventLoopGroup worker = new NioEventLoopGroup();
             bootstrap = new ServerBootstrap();

            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new RpcServerInitializer(nettyConfig, serviceBeanMapping));
            // 绑定到网卡
            ChannelFuture future = bootstrap.bind(nettyConfig.getHost(), nettyConfig.getPort());
            ChannelFuture channelFuture = future.addListener(bindingFuture -> {
                if (bindingFuture.isSuccess()) {
                    logger.info("Server started on port {}", nettyConfig.getPort());
                } else {
                    logger.error("Server start failed");
                    stop();
                }
            });
            try {
                channelFuture.await();
                if(channelFuture.isSuccess()){
                    flag = true;
                }
            } catch (InterruptedException e){
                logger.error(e.getMessage(), e);
            }
            return flag;
        }
    }
/*    @Override
    protected void initNettyServer() throws Exception{
        // 由于Netty会阻塞线程，所以此处需要创建一个新的线程来启动netty
        thread = new Thread(()->{
            EventLoopGroup boss = new NioEventLoopGroup(1);
            EventLoopGroup worker = new NioEventLoopGroup();
            try {
                ServerBootstrap  bootstrap = new ServerBootstrap();
                RpcSerializer serializer = nettyConfig.getNewSerializer();
                if(serializer == null){
                    logger.warn("Can't create serializer instance , so we use default serializer");
                    serializer = new JdKSerializer();
                }
                bootstrap.group(boss, worker)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childHandler(new RpcServerInitializer(serializer, serviceBeanMapping, nettyConfig.getThreadPool()));
                ChannelFuture channelFuture = bootstrap.bind(nettyConfig.getHost(), nettyConfig.getPort());
                logger.info("Server started on port {}", nettyConfig.getPort());
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e){
                logger.info("Rpc server remoting server stop");
            } catch (Exception e){
                logger.error("Rpc server remoting server error", e);
            } finally {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            }
        });
        thread.start();
    }*/

/*    @Override
    protected void handleStartFailed() {
        if(worker != null){
            worker.shutdownGracefully();
            worker = null;
        }
        if(boss != null){
            boss.shutdownGracefully();
            boss = null;
        }
    }*/


    @Override
    protected void destroyNettyServer() {
/*        if(thread != null && thread.isAlive()){
            thread.interrupt();
        }*/
/*        if(worker != null){
            worker.shutdownGracefully();
            worker = null;
        }
        if(boss != null){
            boss.shutdownGracefully();
            boss = null;
        }*/
//       nettyConfig.getThreadPool().shutdown();
        if(bootstrap != null){
            EventLoopGroup boss = bootstrap.config().group();
            EventLoopGroup worker = bootstrap.config().childGroup();
            if(boss != null){
                boss.shutdownGracefully();

            }
            if(worker != null){
                worker.shutdownGracefully();
            }
        }
        bootstrap = null;
    }

}
