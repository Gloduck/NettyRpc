package cn.gloduck.netty.rpc.codec;

import cn.gloduck.netty.rpc.enums.MessageType;

/**
 * rpc心跳包
 * @author Gloduck
 */
public final class RpcBeat implements RpcMessage {
    private RpcBeat(){}
    private static final RpcBeat instance = new RpcBeat();
    public static RpcBeat instance(){
        return instance;
    }
    @Override
    public MessageType getMessageType() {
        return MessageType.RPC_BEAT;
    }
}
