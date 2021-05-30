package cn.gloduck.netty.rpc.codec;

import cn.gloduck.netty.rpc.enums.MessageType;

/**
 * RPC返回值
 */
public final class RpcResponse implements RpcMessage {
    /**
     * 执行成功
     */
    public static final int SUCCESS = 0;
    /**
     * 客户端异常
     */
    public static final int CLIENT_FAILED = 1;
    /**
     * 发送消息出现异常
     */
    public static final int SEND_FAILED = 2;
    /**
     * 服务器异常
     */
    public static final int SERVER_FAILED = 3;
    private RpcResponse(){}

    public RpcResponse(String requestId, Integer code, String message, Object data) {
        this.requestId = requestId;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 返回状态码
     */
    private Integer code;
    /**
     * 额外补充
     */
    private String message;
    /**
     * 返回具体信息
     */
    private Object data;

    /**
     * 发送成功
     * @param requestId
     * @param data
     * @return
     */
    public static RpcResponse success(String requestId, Object data){
        return new RpcResponse(requestId, SUCCESS, null, data);
    }

    /**
     * 发送失败
     * @param requestId
     * @param message
     * @return
     */
    public static RpcResponse sendFailed(String requestId, String message){
        return new RpcResponse(requestId, SEND_FAILED, message, null);
    }

    /**
     * 服务端错误
     * @param requestId
     * @param message
     * @return
     */
    public static RpcResponse serverFailed(String requestId, String message){
        return new RpcResponse(requestId, SERVER_FAILED, message, null);
    }

    /**
     * 判断rpc请求是否成功
     * @return
     */
    public boolean success(){
        return this.code.equals(SUCCESS);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.RPC_RESPONSE;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
