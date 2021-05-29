package cn.gloduck.netty.rpc.transport;

import cn.gloduck.netty.rpc.listener.NettyStateListener;
import cn.gloduck.netty.rpc.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public abstract class AbstractNettyServer implements Server {
    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyServer.class);
    protected NettyConfig nettyConfig;
    private List<NettyStateListener> listeners;

    public AbstractNettyServer(NettyConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
    }

    @Override
    public final void start() {
        boolean flag = false;
        try {
            this.initNettyServer();
            flag = true;
        } catch (Exception e) {
            logger.warn("Netty启动失败 , {}", e.getMessage());
            if(!CollectionUtil.isEmptyCollection(listeners)){
                // 发布启动失败时间
                for (NettyStateListener listener : listeners) {
                    listener.onException(e);
                }
            }
        }
        if(flag){
            if(!CollectionUtil.isEmptyCollection(listeners)){
                for (NettyStateListener listener : listeners) {
                    // 发布Netty启动事件
                    listener.onNettyStart(nettyConfig);
                }
            }
        }
    }

    /**
     * 初始化Netty服务器的方法
     */
    protected abstract boolean initNettyServer() throws Exception;

    @Override
    public final void stop() {
        this.destroyNettyServer();
        if(!CollectionUtil.isEmptyCollection(listeners)){
            for (NettyStateListener listener : listeners) {
                // 发布Netty关闭事件
                listener.onNettyStop(nettyConfig);
            }
        }
    }

    /**
     * 添加Netty的监听器
     * @param listener
     */
    public void addListener(NettyStateListener listener){
        if(this.listeners == null){
            this.listeners = new LinkedList<>();
        }
        this.listeners.add(listener);
    }

    /**
     * 初始化Netty服务器失败方法
     */
//    protected abstract void handleStartFailed();

    /**
     * 关闭Netty服务器的方法
     */
    protected abstract void destroyNettyServer();
}
