package cn.gloduck.netty.rpc.exception;

import java.util.concurrent.ExecutionException;

/**
 * rpc远程执行出现异常
 */
public class RpcRemoteInvokeException extends ExecutionException {
    public RpcRemoteInvokeException(String message) {
        super(message);
    }
}
