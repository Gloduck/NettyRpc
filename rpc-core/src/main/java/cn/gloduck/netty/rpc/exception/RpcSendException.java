package cn.gloduck.netty.rpc.exception;

/**
 * rpc发送失败异常
 */
public class RpcSendException extends RpcInvokeException {
    public RpcSendException() {
    }

    public RpcSendException(String message) {
        super(message);
    }

    public RpcSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcSendException(Throwable cause) {
        super(cause);
    }

    public RpcSendException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
