package cn.gloduck.netty.rpc.transport.client;


import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.codec.RpcResponse;
import cn.gloduck.netty.rpc.exception.RpcException;
import cn.gloduck.netty.rpc.exception.RpcInvokeException;
import cn.gloduck.netty.rpc.transport.sync.ResponseFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class Transporter {
    private final static Logger logger = LoggerFactory.getLogger(Transporter.class);
    private int timeout;
    private Channel channel;
    private RpcResponseHandler responseHandler;

    public Transporter(int timeout, Channel channel, RpcResponseHandler responseHandler) {
        this.timeout = timeout;
        this.channel = channel;
        this.responseHandler = responseHandler;
    }

    /**
     * 销毁当前连接
     */
    public void destroy(){
        channel.close();
    }
    public boolean isAvailable(){
        return channel != null && channel.isOpen() && channel.isActive();
    }

    public Channel getChannel(){
        return channel;
    }

    protected void checkConnection(){
        if(!isAvailable()){
            throw new RpcInvokeException("channel is not available");
        }
    }
    public Object syncSend(RpcRequest request){
        return syncSend(request, timeout);
    }

    public Object syncSend(RpcRequest request, int timeout){
        checkConnection();
        Object data = null;
        try {
            ResponseFuture future = doSend(request);
            data = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (RpcInvokeException e){
            throw e;
        } catch (Exception e){
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return data;
    }



    public ResponseFuture asyncSend(RpcRequest request){
        checkConnection();
        ResponseFuture future = null;
        try {
             future = doSend(request);
        } catch (RpcInvokeException e){
            throw e;
        } catch (Exception e){
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return future;
    }


    protected ResponseFuture doSend(final RpcRequest request){
        ChannelFuture write = channel.write(request);
        write.addListener(future -> {
            if(future.isDone()){
                if(future.cause() != null){
                    // 发送请求出现错误
                    logger.error("发送RPC请求时候出现错误", future.cause());
                }
                if(future.isCancelled()){
                    logger.warn("RPC请求被取消");
                }
            }

        });
        ResponseFuture future = responseHandler.registryProcessRequest(request);
        channel.flush();
        return future;
    }
}
