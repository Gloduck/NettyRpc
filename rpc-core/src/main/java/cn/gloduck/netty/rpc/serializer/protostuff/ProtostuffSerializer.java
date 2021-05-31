package cn.gloduck.netty.rpc.serializer.protostuff;

import cn.gloduck.netty.rpc.exception.SerializationException;
import cn.gloduck.netty.rpc.serializer.RpcSerializer;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * protobuf序列化器
 *
 * @author Gloduck
 */
public class ProtostuffSerializer implements RpcSerializer {
    private final Map<Class<?>, Schema<?>> cachedSchema;
    private final Objenesis objenesis;

    public ProtostuffSerializer() {
        cachedSchema = new ConcurrentHashMap<>(16);
        objenesis = new ObjenesisStd(true);
    }
    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> cls) {
        // for thread-safe
        return (Schema<T>) cachedSchema.computeIfAbsent(cls, RuntimeSchema::createFrom);
    }
    @Override
    public <T extends Serializable> byte[] encode(T obj) throws SerializationException {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new SerializationException(e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T extends Serializable> T decode(byte[] bytes, Class<T> classz) throws SerializationException {
        try {
            T message = (T) objenesis.newInstance(classz);
            Schema<T> schema = getSchema(classz);
            ProtostuffIOUtil.mergeFrom(bytes, message, schema);
            return message;
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    @Override
    public byte serializerTypeCode() {
        return 3;
    }
}
