package cn.gloduck.netty.rpc.transport.server;

import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.codec.RpcResponse;
import cn.gloduck.netty.rpc.exception.RpcInvokeException;
import cn.gloduck.netty.rpc.ref.server.BeanAndMethod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Gloduck
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final static Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
    private final Map<String, BeanAndMethod> serviceBeanMapping;
    /**
     * 执行任务的线程池
     */
    private final ThreadPoolExecutor executor;

    public RpcServerHandler(Map<String, BeanAndMethod> serviceBeanMapping, ThreadPoolExecutor executor) {
        this.serviceBeanMapping = serviceBeanMapping;
        this.executor = executor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)  {
        logger.info("RPC服务端创建了一个新的连接");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        logger.info("收到RPC请求，请求ID为 : {} , 请求服务名为 : {}", msg.getRequestId(), msg.getServiceName());
        executor.submit(() -> {
            logger.info("开始执行RPC请求 : {}", msg.getRequestId());
            RpcResponse response;
            try {
                // 执行rpc任务成功
                response = handleRequest(msg);
            } catch (Throwable e) {
                logger.error("执行RPC请求出现错误", e);
                response = RpcResponse.serverFailed(msg.getRequestId(), e.getMessage());
            }
            // 执行任务成功，返回消息
            // 注：此处可能有线程安全问题，待探究
            ctx.writeAndFlush(response)
                    .addListener(future -> {
                        if(future.isDone()){
                            Throwable cause = future.cause();
                            if(cause != null){
                                logger.error("返回RPC数据出现错误");
                                // 返回数据出现错误，尝试发送一条发送失败的消息
                                ctx.writeAndFlush(RpcResponse.sendFailed(msg.getRequestId(),cause.getMessage()));
                            }
                        }
                    });
        });
    }

    /**
     * 调用方法
     * @param request
     * @return
     */
    public RpcResponse handleRequest(RpcRequest request) {
        String serviceName = request.getServiceName();
        BeanAndMethod beanAndMethod = serviceBeanMapping.get(serviceName);
        if (beanAndMethod == null) {
            // 如果没有找到对应的service
            String msg = String.format("当前RPC服务端没有对应的服务 %s", serviceName);
            return RpcResponse.serverFailed(request.getRequestId(), msg);
        }
        RpcResponse response;
        try {
            Object result = beanAndMethod.invokeMethod(request.getParameters(), request.getParameterTypes());
            response = RpcResponse.success(request.getRequestId(), result);
        } catch (RpcInvokeException e){
            logger.warn("执行RPC请求出现错误, {}",e.getMessage());
            response = RpcResponse.serverFailed(request.getRequestId(), e.getMessage());
        }
        return response;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)  {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("RPC服务出现异常: " + cause.getMessage());
    }

}
