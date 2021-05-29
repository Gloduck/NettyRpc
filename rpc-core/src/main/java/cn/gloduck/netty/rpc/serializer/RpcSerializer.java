package cn.gloduck.netty.rpc.serializer;

import cn.gloduck.netty.rpc.enums.Serializers;
import cn.gloduck.netty.rpc.exception.SerializationException;

import java.io.Serializable;

public interface RpcSerializer {
    /**
     * 解码
     */
    <T extends Serializable> byte[] encode(T obj) throws  SerializationException;

    /**
     * 转码
     */
    <T extends Serializable> T decode(byte[] bytes, Class<T> classz) throws SerializationException;


     byte serializerTypeCode();
}
