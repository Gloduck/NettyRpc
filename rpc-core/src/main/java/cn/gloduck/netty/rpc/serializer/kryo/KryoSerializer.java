package cn.gloduck.netty.rpc.serializer.kryo;

import cn.gloduck.netty.rpc.exception.SerializationException;
import cn.gloduck.netty.rpc.serializer.RpcSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class KryoSerializer implements RpcSerializer {
    private final KryoPool pool = KryoPoolFactory.getKryoPoolInstance();
    @Override
    public <T extends Serializable> byte[] encode(T obj) throws SerializationException {
        Kryo kryo = pool.borrow();
        byte[] bytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); Output output = new Output(outputStream)){
            kryo.writeObject(output, obj);
            output.flush();
            bytes = outputStream.toByteArray();
        } catch (IOException e){
            throw new SerializationException(e);
        } finally {
            pool.release(kryo);
        }
        return bytes;
    }

    @Override
    public <T extends Serializable> T decode(byte[] bytes, Class<T> classz) throws SerializationException {
        Kryo kryo = pool.borrow();
        T obj;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes); Input input = new Input(inputStream)){
            obj = kryo.readObject(input, classz);
        } catch (IOException e){
            throw new SerializationException(e);
        } finally {
            pool.release(kryo);
        }
        return obj;
    }

    @Override
    public byte serializerTypeCode() {
        return 2;
    }
}
