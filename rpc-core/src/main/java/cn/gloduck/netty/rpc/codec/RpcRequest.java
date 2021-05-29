package cn.gloduck.netty.rpc.codec;

import cn.gloduck.netty.rpc.enums.MessageType;

import java.util.Arrays;


public class RpcRequest implements RpcMessage {
    /**
     * 请求号
     */
    private String requestId;
    /**
     * 待调用接口名称
     */
//    private String interfaceName;
    /**
     * 调用方法的名称
     */
//    private String methodName;
    private String serviceName;

    /**
     * 调用方法的参数
     */
    private Object[] parameters;
    /**
     * 调用方法的参数类型
     */
    private Class<?>[] parameterTypes;


    @Override
    public MessageType getMessageType() {
        return MessageType.RPC_REQUEST;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
/*
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
*/

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                '}';
    }
}
