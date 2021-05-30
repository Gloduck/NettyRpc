package cn.gloduck.netty.rpc.transport.client;

import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.codec.RpcResponse;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gloduck
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcResponseHandler.class);
    /**
     * 正在处理中的rpc任务
     */
    private final Map<String, ResponseFuture> processingRpcRequest;

    public RpcResponseHandler() {
        this.processingRpcRequest = new ConcurrentHashMap<>(16);
    }

    /**
     * 注册当前任务为正在执行的任务
     * @return
     */
    public ResponseFuture registryProcessRequest(RpcRequest request){
        ResponseFuture future = new ResponseFuture(request, this);
        this.processingRpcRequest.put(request.getRequestId(), future);
        return future;
    }

    public ResponseFuture removeRequest(String requestId){
        return processingRpcRequest.remove(requestId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        String requestId = msg.getRequestId();
        logger.info("收到服务ID为：{} 的响应",requestId);
        ResponseFuture responseFuture = removeRequest(requestId);
        if(responseFuture == null){
            logger.warn("收到未知的服务ID : {}",requestId);
        } else {
            responseFuture.receiveResponse(msg);
        }
    }




    /**
     * 出现异常关闭通道
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("客户端发生异常 : {}", cause.getMessage());
    }

    /**
     * 自定义心跳
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }


}
