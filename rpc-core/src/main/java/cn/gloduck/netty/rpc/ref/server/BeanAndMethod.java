package cn.gloduck.netty.rpc.ref.server;

import cn.gloduck.netty.rpc.exception.RpcInvokeException;
import cn.gloduck.netty.rpc.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 保存bean和method的映射
 * @author Gloduck
 */
public class BeanAndMethod {
    private final static Logger logger = LoggerFactory.getLogger(BeanAndMethod.class);
    private Object bean;
    private Method method;

    public BeanAndMethod(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public Object invokeMethod(Object[] parameters,Class<?>[] requireParameterTypes){
        Class<?>[] parameterTypes = method.getParameterTypes();
        logger.info("尝试执行请求的方法 , 请求的参数为： {}, 请求的类型为： {}, 方法的实际类型为： {}",Arrays.toString(parameters), Arrays.toString(requireParameterTypes), Arrays.toString(parameterTypes));
        if(!Arrays.equals(requireParameterTypes, parameterTypes)){
            // 对参数类型进行验证，确保就是要执行的方法。
            String msg = String.format("请求的方法类型 : %s 和实际的方法类型不匹配 : %s", Arrays.toString(requireParameterTypes), Arrays.toString(parameterTypes));
            throw new RpcInvokeException(msg);
        }
        Object result;
        try {
            // 由于注册的方法都是共有的方法，所以应该不会出现权限问题。
            result = method.invoke(bean, parameters);
        } catch (ReflectiveOperationException e) {
            throw new RpcInvokeException(e.getMessage());
        }
        return result;
    }

    @Override
    public String toString() {
        return "BeanAndMethod{" +
                "bean=" + bean +
                ", method=" + method +
                '}';
    }
}
