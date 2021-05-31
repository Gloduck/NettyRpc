package cn.gloduck.netty.rpc.serializer;

import io.netty.util.internal.ObjectUtil;

import java.lang.reflect.Constructor;

/**
 * 序列化器工厂
 * @author Gloduck
 */
public class SerializerFactory<T extends RpcSerializer> {
    private final Constructor<? extends T> constructor;

    public SerializerFactory(Class<? extends T> serializerClass) {
        ObjectUtil.checkNotNull(serializerClass, "serializerClass");
        try {
            this.constructor = serializerClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + serializerClass.getSimpleName() + "does not have a public non-arg constructor", e);
        }
    }
    public T newInstance(){
        try {
            return constructor.newInstance();
        } catch (Throwable t) {
            throw new IllegalArgumentException("Unable to create serializer from class " + constructor.getDeclaringClass(), t);
        }
    }
}
