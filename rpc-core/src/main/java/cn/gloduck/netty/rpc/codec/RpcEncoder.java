package cn.gloduck.netty.rpc.codec;

import cn.gloduck.netty.rpc.constant.RpcConstant;
import cn.gloduck.netty.rpc.serializer.RpcSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 编码器
 * 协议格式：magic（协议魔数），序列化器，消息类型，数据长度，数据
 * @author Gloduck
 */
public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {
    private final static Logger logger = LoggerFactory.getLogger(RpcEncoder.class);
    private final RpcSerializer serializer;

    public RpcEncoder(RpcSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        // 写文件魔数
        out.writeBytes(RpcConstant.MAGIC_NUMBER);
        // 写序列化器
        byte serializerTypeCode = serializer.serializerTypeCode();
        out.writeByte(serializerTypeCode);
        // 写消息类型
        byte messageTypeCode = msg.getMessageType().getTypeCode();
        out.writeByte(messageTypeCode);
        // 编码数据
        byte[] data = serializer.encode(msg);
        // 写数据长度
        out.writeInt(data.length);
        // 写数据
        out.writeBytes(data);
    }
}
