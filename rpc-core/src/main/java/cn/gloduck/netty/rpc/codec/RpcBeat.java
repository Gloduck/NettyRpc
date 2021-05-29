package cn.gloduck.netty.rpc.codec;

import cn.gloduck.netty.rpc.enums.MessageType;

/**
 * rpc心跳包
 * @author Gloduck
 */
public final class RpcBeat implements RpcMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.RPC_BEAT;
    }
}
