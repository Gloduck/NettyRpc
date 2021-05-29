package cn.gloduck.netty.rpc.serializer.jdk;

import cn.gloduck.netty.rpc.exception.SerializationException;
import cn.gloduck.netty.rpc.serializer.RpcSerializer;
import cn.gloduck.netty.rpc.enums.Serializers;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;

public class JdKSerializer implements RpcSerializer {
    @Override
    public <T extends Serializable> byte[] encode(T obj) throws SerializationException {
        byte[] bytes = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(obj);
            bytes = outputStream.toByteArray();
        }catch (NotSerializableException e) {
            throw new SerializationException("当前对象无法被序列化，可能成员变量没有实现Serializable接口");
        }catch(IOException e) {
            throw new SerializationException(e.getMessage(), e.getCause());
        }
        return bytes;

    }

    @Override
    public <T extends Serializable> T decode(byte[] bytes, Class<T> classz) throws SerializationException {
        T res = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes); ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            Object readObject = objectInputStream.readObject();
            if (!classz.isInstance(readObject)) {
                throw new SerializationException("类型不匹配");

            } else {
                res = (T) readObject;
            }
        }catch (NotSerializableException e) {
            throw new SerializationException("当前对象无法被反序列化，可能成员变量没有实现Serializable接口");
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException(e.getMessage(), e.getCause());
        }

        return res;
    }

    @Override
    public byte serializerTypeCode() {
        return 0;
    }

}
