package cn.gloduck.netty.rpc.transport.server;

import cn.gloduck.netty.rpc.codec.RpcBeat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端心跳处理器
 * @author Gloduck
 */
public class RpcServerHeartBeatHandler extends SimpleChannelInboundHandler<RpcBeat> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHeartBeatHandler.class);
    private int counter;
    private int limit;

    public RpcServerHeartBeatHandler(int limit) {
        this.counter = 0;
        this.limit = limit;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcBeat msg) {
        // 收到心跳后重置心跳计数器
        this.counter = 0;
        logger.info("收到心跳，重置计数器");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt){
        if(evt instanceof IdleStateEvent){
            counter++;
        }
        if(counter >= limit){
            logger.warn("连接超时[长时间未收到心跳]，连接关闭");
            ctx.disconnect();
        }
    }
}
