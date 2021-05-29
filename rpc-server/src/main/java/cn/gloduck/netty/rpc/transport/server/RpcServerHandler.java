package cn.gloduck.netty.rpc.transport.server;

import cn.gloduck.netty.rpc.codec.RpcMessage;
import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.codec.RpcResponse;
import cn.gloduck.netty.rpc.exception.RpcException;
import cn.gloduck.netty.rpc.exception.RpcInvokeException;
import cn.gloduck.netty.rpc.ref.server.BeanAndMethod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Receive a new connection");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        logger.info("receive rpc request , requestId : {} , serviceName : {}", msg.getRequestId(), msg.getServiceName());
        executor.submit(() -> {
            logger.info("stater to execute rpc service : {}", msg.getServiceName());
            RpcResponse response;
            try {
                // 执行rpc任务成功
                response = handleRequest(msg);
            } catch (Throwable e) {
                logger.error("RPC Server handle request error", e);
                response = RpcResponse.failed(msg.getRequestId(), e.getMessage());
            }
            // 执行任务成功，返回消息
            // 注：此处可能有线程安全问题，待探究
            ctx.writeAndFlush(response)
                    .addListener(future -> {
                        if(future.isDone()){
                            if(future.cause() != null){
                                logger.error("返回RPC数据出现错误",future.cause());
                            }
                            if(future.isCancelled()){
                                logger.warn("返回数据被取消");
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
            String msg = String.format("No bean contains service %s", serviceName);
            return RpcResponse.failed(request.getRequestId(), msg);
        }
        RpcResponse response;
        try {
            Object result = beanAndMethod.invokeMethod(request.getParameters(), request.getParameterTypes());
            response = RpcResponse.success(request.getRequestId(), result);
        } catch (RpcInvokeException e){
            logger.warn("Invoke method failed , {}",e.getMessage());
            response = RpcResponse.failed(request.getRequestId(), e.getMessage());
        }
        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Server caught exception: " + cause.getMessage());
        cause.printStackTrace();
//        ctx.close();
    }

}
