package cn.gloduck.netty.rpc.listener;

import cn.gloduck.netty.rpc.transport.NettyConfig;

/**
 * netty状态改变的监听器
 * @author Gloduck
 */
public interface NettyStateListener {
    /**
     * Netty启动事件
     * @param config
     */
    void onNettyStart(NettyConfig config);

    /**
     * Netty停止事件
     * @param config
     */
    void onNettyStop(NettyConfig config);

    /**
     * Netty启动出错事件
     * @param e
     */
    void onException(Throwable e);
}
