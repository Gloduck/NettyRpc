package cn.gloduck.netty.rpc.enums;

import cn.gloduck.netty.rpc.codec.RpcMessage;
import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.codec.RpcResponse;

/**
 * 消息类型，和RpcMessage双向绑定
 *
 * @author Gloduck
 */
public enum MessageType {
    /**
     * rpc响应
     */
    RPC_RESPONSE {
        @Override
        public byte getTypeCode() {
            return 2;
        }

        @Override
        public Class<? extends RpcMessage> getBindingClassType() {
            return RpcResponse.class;
        }
    },
    /**
     * rpc请求
     */
    RPC_REQUEST{
        @Override
        public byte getTypeCode() {
            return 1;
        }
        @Override
        public Class<? extends RpcMessage> getBindingClassType() {
            return RpcRequest.class;
        }
    },
    /**
     * rpc心跳
     */
    RPC_BEAT{
        @Override
        public byte getTypeCode() {
            return 0;
        }
/*        @Override
        public Class<? extends RpcMessage> getBindingClassType() {
            return RpcResponse.class;
        }*/
    };

    /**
     * 获取类型字节
     *
     * @return
     */
    public byte getTypeCode() {
        throw new AbstractMethodError();
    }

    /**
     * 获取绑定的Class类型
     * @return
     */
    public Class<? extends RpcMessage> getBindingClassType(){
        throw new AbstractMethodError();
    }

    /**
     * 通过类型字节获取消息的类型
     *
     * @return
     */
    public static MessageType getMessageTypeByTypeCode(byte typeCode) {
        MessageType[] values = MessageType.values();
        for (MessageType value : values) {
            if (typeCode == value.getTypeCode()) {
                return value;
            }
        }
        return null;
    }
}
