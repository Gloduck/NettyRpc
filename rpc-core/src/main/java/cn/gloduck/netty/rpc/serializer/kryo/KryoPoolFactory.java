package cn.gloduck.netty.rpc.serializer.kryo;

import cn.gloduck.netty.rpc.codec.RpcBeat;
import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.codec.RpcResponse;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class KryoPoolFactory {
    private static volatile KryoPoolFactory poolFactory = null;

    private KryoFactory factory = () -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        kryo.register(RpcBeat.class);
        kryo.register(RpcBeat.class);
        Kryo.DefaultInstantiatorStrategy strategy = (Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy();
        strategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    };

    private KryoPool pool = new KryoPool.Builder(factory).build();

    private KryoPoolFactory() {
    }

    public static KryoPool getKryoPoolInstance() {
        if (poolFactory == null) {
            synchronized (KryoPoolFactory.class) {
                if (poolFactory == null) {
                    poolFactory = new KryoPoolFactory();
                }
            }
        }
        return poolFactory.getPool();
    }

    public KryoPool getPool() {
        return pool;
    }
}
