package cn.gloduck.netty.rpc.transport.client;

import cn.gloduck.netty.rpc.codec.RpcBeat;
import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.codec.RpcResponse;

import cn.gloduck.netty.rpc.transport.NettyConfig;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gloduck
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcResponseHandler.class);
    /**
     * 连接的远程服务器的地址
     */
    private final String remoteAddress;
    /**
     * 正在处理中的rpc任务
     */
    private final Map<String, ResponseFuture> processingRpcRequest;

    public RpcResponseHandler(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        this.processingRpcRequest = new ConcurrentHashMap<>(16);
    }

    /**
     * 注册当前任务为正在执行的任务
     *
     * @return
     */
    public ResponseFuture registryProcessRequest(RpcRequest request) {
        ResponseFuture future = new ResponseFuture(request, this);
        this.processingRpcRequest.put(request.getRequestId(), future);
        return future;
    }

    public ResponseFuture removeRequest(String requestId) {
        return processingRpcRequest.remove(requestId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) {
        String requestId = msg.getRequestId();
        logger.info("收到服务ID为：{} 的响应", requestId);
        // 收到响应后，将当前请求从正在请求的任务中移除
        ResponseFuture responseFuture = removeRequest(requestId);
        if (responseFuture == null) {
            logger.warn("收到未知的服务ID : {}", requestId);
        } else {
            // 收到消息，这个消息可能是调用失败的消息，也可能是调用成功的消息。在使用ResponseFuture#get的时候，如果是失败消息则直接根据失败的类型抛出异常。
            responseFuture.receiveResponse(msg);
        }
    }


    /**
     * 出现异常关闭通道
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
        logger.error("客户端发生异常 : {}", cause.getMessage());
    }

    /**
     * 自定义心跳，每到一定时间{@link NettyConfig#getHeartBeatInterval()}，就会触发时间，然后发送间隔。
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            Channel channel = ctx.channel();
            channel.writeAndFlush(RpcBeat.instance());
            logger.info("向服务器：{} 发送心跳",remoteAddress);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // 删除连接
        ConnectionManager.instance().removeTransporter(remoteAddress);
        // 取消所有任务
        if(!this.processingRpcRequest.isEmpty()){
            Set<Map.Entry<String, ResponseFuture>> entries =
                    this.processingRpcRequest.entrySet();
            Iterator<Map.Entry<String, ResponseFuture>> iterator = entries.iterator();
            while (iterator.hasNext()){
                Map.Entry<String, ResponseFuture> next = iterator.next();
                ResponseFuture value = next.getValue();
                value.cancel(false);
                logger.warn("因为连接关闭，任务被取消，请求ID为：{}",next.getKey());
                iterator.remove();
            }
        }
        logger.warn("关闭连接：{}",remoteAddress);
    }


}
