package cn.gloduck.netty.rpc.exception;

/**
 * rpc调用时的异常
 */
public class RpcInvokeException extends RuntimeException {



    public RpcInvokeException() {
        super();
    }

    public RpcInvokeException(String message) {
        super(message);
    }

    public RpcInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcInvokeException(Throwable cause) {
        super(cause);
    }

    public RpcInvokeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
