package cn.gloduck.netty.rpc.transport.client;


import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.codec.RpcResponse;
import cn.gloduck.netty.rpc.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author Gloduck
 */
public class ResponseFuture<T> implements Future<T> {
    private final static Logger logger = LoggerFactory.getLogger(ResponseFuture.class);
    private final String requestId;
    private final String serviceName;
    private RpcResponse response;
    private RpcResponseHandler handler;
    private final Sync sync;
    private volatile boolean cancel;
/*    private ExceptionListener exceptionListener;
    private ReceiveMessageListener<T> receiveMessageListener;*/

    void receiveResponse(RpcResponse response) {
        this.response = response;
        sync.release(1);
//        notifyListener(response);
    }


    public ResponseFuture(RpcRequest request, RpcResponseHandler handler) {
        this.handler = handler;
        this.requestId = request.getRequestId();
        this.serviceName = request.getServiceName();
        this.sync = new Sync();
        this.cancel = false;
    }

 /*   private void notifyListener(RpcResponse response){
        if(response.success()){
            // 如果成功
            if(this.receiveMessageListener != null){
                Object data = response.getData();
                try {
                    T res = (T) data;
                    receiveMessageListener.handleMessageReceive(res);
                } catch (ClassCastException e){
                    if(this.exceptionListener != null){
                        exceptionListener.handleException(e);
                    }
                }
            }
        } else {
            if(this.exceptionListener != null){
                switch (response.getCode()){
                    case RpcResponse.CLIENT_FAILED:
                        this.exceptionListener.handleException(new RpcClientInvokeException(response.getMessage()));
                        break;
                    case RpcResponse.SEND_FAILED:
                        this.exceptionListener.handleException(new RpcSendException(response.getMessage()));
                        break;
                    case RpcResponse.SERVER_FAILED:
                        this.exceptionListener.handleException(new RpcServerInvokeException(response.getMessage()));
                        break;
                    default:
                        break;
                }
            }
        }
    }*/

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public boolean cancel() {
        boolean release = sync.release(1);
        return release;
    }
/*
    public ResponseFuture<T> onException(ExceptionListener listener) {
        this.exceptionListener = listener;
        return this;
    }*/


/*    public ResponseFuture<T> onReceiveMessage(ReceiveMessageListener listener) {
        this.receiveMessageListener = listener;
        return this;
    }*/

    @Override
    public T get() throws InterruptedException, RpcInvokeException {
        sync.acquire(1);
        if (this.response != null) {
            if (response.success()) {
                return (T) response.getData();
            } else {
                switch (response.getCode()) {
                    // 远程发送错误
                    case RpcResponse.SEND_FAILED:
                        throw new RpcSendException(response.getMessage());
                    // 远程执行错误
                    case RpcResponse.SERVER_FAILED:
                        throw new RpcServerInvokeException(response.getMessage());
                    default:
                        return null;
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, RpcInvokeException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if (success) {
            if (response == null) {
                // 没有获取到结果
                return null;
            } else {
                if (response.success()) {
                    // 执行成功
                    return (T) response.getData();
                } else {
                    // 远程执行错误
                    switch (response.getCode()) {
                        // 远程发送错误
                        case RpcResponse.SEND_FAILED:
                            throw new RpcSendException(response.getMessage());
                            // 远程执行错误
                        case RpcResponse.SERVER_FAILED:
                            throw new RpcServerInvokeException(response.getMessage());
                        default:
                            return null;
                    }                }
            }
        } else {
            String msg = String.format("RPC服务获取响应超时, 请求ID为 : %s, 请求的服务为 : %s", requestId, serviceName);
            throw new TimeoutException(msg);
        }
    }

    private static class Sync extends AbstractQueuedSynchronizer {
        private final static int DONE_STATE = 1;
        private final static int PENDING_STATE = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == DONE_STATE;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == PENDING_STATE) {
                if (compareAndSetState(PENDING_STATE, DONE_STATE)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        protected boolean isDone() {
            return DONE_STATE == getState();
        }
    }

/*    @FunctionalInterface
    public interface ExceptionListener{
        void handleException(Throwable e);
    }

    @FunctionalInterface
    public interface ReceiveMessageListener<T>{
        void handleMessageReceive(T data);
    }*/
}
