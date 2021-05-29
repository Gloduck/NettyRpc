package cn.gloduck.netty.rpc.codec;

import cn.gloduck.netty.rpc.constant.RpcConstant;
import cn.gloduck.netty.rpc.enums.MessageType;
import cn.gloduck.netty.rpc.enums.Serializers;
import cn.gloduck.netty.rpc.exception.SerializationException;
import cn.gloduck.netty.rpc.serializer.RpcSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {
    /**
     * 基本大小，必须大于这个大小才接收。大小为：魔数 + 序列化类型 + 消息类型 + 长度（int 4个字节）
     */
    private static final int BASE_SIZE = RpcConstant.MAGIC_NUMBER.length + 1 + 1 + 4;
    private static final Logger logger = LoggerFactory.getLogger(RpcDecoder.class);
    private final RpcSerializer serializer;

    public RpcDecoder( RpcSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] magic = new byte[RpcConstant.MAGIC_NUMBER.length];
        // 读取魔数
        in.readBytes(magic);
        if(!Arrays.equals(magic, RpcConstant.MAGIC_NUMBER)){
            final String msg = "错误的魔数";
            logger.error(msg);
            throw new SerializationException(msg);
        }
        // 读取序列化器
        byte serializerTypeCode = in.readByte();
        if(serializerTypeCode != serializer.serializerTypeCode()){
            final String msg = "错误的序列化器类型，或序列化器不匹配";
            logger.error(msg);
            throw new SerializationException(msg);
        }
        // 读取消息类型
        byte messageTypeCode = in.readByte();
        MessageType messageTypeByTypeCode = MessageType.getMessageTypeByTypeCode(messageTypeCode);
        if(messageTypeByTypeCode == null){
            final String msg = "错误的消息类型";
            logger.error(msg);
            throw new SerializationException(msg);
        }
        // 读取消息长度
        int length = in.readInt();
        if(length <= 0){
            final String msg = "错误的消息长度";
            logger.error(msg);
            throw new SerializationException(msg);
        }
        Class<? extends RpcMessage> bindingClassType = messageTypeByTypeCode.getBindingClassType();
        // 读取消息具体内容
        byte[] data = new byte[length];
        in.readBytes(data);
        RpcMessage decode = serializer.decode(data, bindingClassType);
        out.add(decode);
    }
}
