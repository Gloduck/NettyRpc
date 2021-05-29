package cn.gloduck.netty.rpc.transport.sync;


import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.codec.RpcResponse;
import cn.gloduck.netty.rpc.exception.RpcRemoteInvokeException;
import cn.gloduck.netty.rpc.transport.client.RpcResponseHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author Gloduck
 */
public class ResponseFuture implements Future<Object> {
    private String requestId;
    private String serviceName;
    private RpcResponse response;
    private RpcResponseHandler handler;
    private Sync sync;
    private volatile boolean cancel;

    public void receiveResponse(RpcResponse response){
        this.response = response;
        sync.release(1);
    }
    public ResponseFuture(RpcRequest request, RpcResponseHandler handler) {
        this.handler = handler;
        this.requestId = request.getRequestId();
        this.serviceName = request.getServiceName();
        this.sync = new Sync();
        this.cancel = false;

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        ResponseFuture future = handler.removeRequest(requestId);
        sync.release(1);
        return cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1);
        if (this.response != null) {
            if(response.success()){
                // 执行成功
                return response.getData();
            } else {
                // 远程执行错误
                throw new RpcRemoteInvokeException(response.getMessage());
            }
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if(success){
            if(response == null){
                // 没有获取到结果
                return null;
            } else {
                if(response.success()){
                    // 执行成功
                    return response.getData();
                } else {
                    // 远程执行错误
                    throw new RpcRemoteInvokeException(response.getMessage());
                }
            }
        } else {
            String msg = String.format("Timeout to get request for rpc request, requestId : %s, method : %s",requestId, serviceName);
            throw new TimeoutException(msg);
        }
    }
    private static class Sync extends AbstractQueuedSynchronizer{
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
        protected boolean isDone(){
            return DONE_STATE == getState();
        }
    }
}
