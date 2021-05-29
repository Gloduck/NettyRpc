package cn.gloduck.netty.rpc.codec;

import cn.gloduck.netty.rpc.enums.MessageType;

/**
 * RPC返回值
 */
public class RpcResponse implements RpcMessage {
    private static final Integer SUCCESS_CODE = 0;
    private static final Integer FAILED_CODE = 1;
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

    public static RpcResponse success(String requestId, Object data){
        return new RpcResponse(requestId, SUCCESS_CODE, null, data);
    }

    public static RpcResponse failed(String requestId, String message){
        return new RpcResponse(requestId, FAILED_CODE, message, null);
    }

    /**
     * 判断rpc请求是否成功
     * @return
     */
    public boolean success(){
        return this.code.equals(SUCCESS_CODE);
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
