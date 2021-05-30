package cn.gloduck.netty.rpc.exception;

/**
 * rpc本地执行异常
 */
public class RpcClientInvokeException extends RpcInvokeException {
    public RpcClientInvokeException(String message) {
        super(message);
    }

    public RpcClientInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcClientInvokeException(Throwable cause) {
        super(cause);
    }

    public RpcClientInvokeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
