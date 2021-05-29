package cn.gloduck.netty.rpc.codec;

import cn.gloduck.netty.rpc.enums.MessageType;

import java.io.Serializable;

/**
 * 消息总接口，和MessageType双向绑定
 * @author Gloduck
 */
public interface RpcMessage extends Serializable {
    /**
     * 获取消息类型枚举
     * @return
     */
    MessageType getMessageType();
}
