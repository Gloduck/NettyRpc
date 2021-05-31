import cn.gloduck.netty.rpc.codec.RpcResponse;
import cn.gloduck.netty.rpc.exception.SerializationException;
import cn.gloduck.netty.rpc.serializer.RpcSerializer;
import cn.gloduck.netty.rpc.serializer.SerializerFactory;
import cn.gloduck.netty.rpc.serializer.jdk.JdKSerializer;
import cn.gloduck.netty.rpc.serializer.json.FastJsonSerializer;
import cn.gloduck.netty.rpc.serializer.kryo.KryoSerializer;
import cn.gloduck.netty.rpc.serializer.protostuff.ProtostuffSerializer;
import org.junit.Test;

public class SimpleTest {
    @Test
    public void serializerTest() throws SerializationException {
        int loop = 20000;
        RpcResponse response = RpcResponse.success("hello", "123");

        RpcSerializer serializer = new SerializerFactory<>(JdKSerializer.class).newInstance();
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            byte[] hellos = serializer.encode(RpcResponse.success("hello", "123"));
            RpcResponse decode = serializer.decode(hellos, RpcResponse.class);
        }
        long end = System.currentTimeMillis();
        System.out.printf("JDK序列化%d次耗时为：%d\n", loop, end - start);
        System.out.printf("JDK编码对象 %s 后大小为：%d字节\n",response, serializer.encode(response).length);
        serializer = new SerializerFactory<>(FastJsonSerializer.class).newInstance();
        start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            byte[] hellos = serializer.encode(RpcResponse.success("hello", "123"));
            RpcResponse decode = serializer.decode(hellos, RpcResponse.class);
        }
         end = System.currentTimeMillis();
        System.out.printf("FastJson序列化%d次耗时为：%d\n", loop, end - start);
        System.out.printf("FastJson编码对象 %s 后大小为：%d字节\n",response, serializer.encode(response).length);

        serializer = new SerializerFactory<>(ProtostuffSerializer.class).newInstance();
        start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            byte[] hellos = serializer.encode(RpcResponse.success("hello", "123"));
            RpcResponse decode = serializer.decode(hellos, RpcResponse.class);
        }
        end = System.currentTimeMillis();
        System.out.printf("Protobuf序列化%d次耗时为：%d\n", loop, end - start);
        System.out.printf("Protobuf编码对象 %s 后大小为：%d字节\n",response, serializer.encode(response).length);

        serializer = new SerializerFactory<>(KryoSerializer.class).newInstance();
        start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            byte[] hellos = serializer.encode(RpcResponse.success("hello", "123"));
            RpcResponse decode = serializer.decode(hellos, RpcResponse.class);
        }
        end = System.currentTimeMillis();
        System.out.printf("Kryo序列化%d次耗时为：%d\n", loop, end - start);
        System.out.printf("Kryo编码对象 %s 后大小为：%d字节\n",response, serializer.encode(response).length);

    }
}
