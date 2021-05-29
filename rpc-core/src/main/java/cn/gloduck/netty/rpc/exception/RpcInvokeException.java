package cn.gloduck.netty.rpc.exception;

/**
 * rpc调用时的异常
 */
public class RpcInvokeException extends RuntimeException {

    private String serviceName;


    public RpcInvokeException() {
        super();
    }

    public RpcInvokeException(String message) {
        super(message);
    }

    public RpcInvokeException(String message, String serviceName) {
        super(message);
        this.serviceName = serviceName;
    }

    public RpcInvokeException(String message, String serviceName, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }
}
