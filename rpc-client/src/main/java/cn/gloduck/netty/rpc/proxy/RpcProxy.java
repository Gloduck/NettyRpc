package cn.gloduck.netty.rpc.proxy;

import cn.gloduck.netty.rpc.annotation.RpcReference;
import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.exception.RpcInvokeException;
import cn.gloduck.netty.rpc.loadbance.LoadBlance;
import cn.gloduck.netty.rpc.transport.client.Transporter;
import cn.gloduck.netty.rpc.transport.client.ConnectionManager;
import cn.gloduck.netty.rpc.ref.client.Instance;
import cn.gloduck.netty.rpc.transport.client.ResponseFuture;
import cn.gloduck.netty.rpc.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

/**
 * @author Gloduck
 */
public class RpcProxy implements InvocationHandler {
    private final static Logger logger = LoggerFactory.getLogger(RpcProxy.class);
    // 获取channel失败默认重试的次数
    private final int DEFAULT_RETRY_COUNTS = 5;
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        RpcReference annotation = method.getAnnotation(RpcReference.class);
        if(annotation == null){
            // 注：此处待完善
            String msg = "RPC方法需要使用RpcReference注解";
            logger.warn(msg);
            throw new AbstractMethodError(msg);
        }
        LoadBlance loadBlance = annotation.loadBlance();
        String serviceName = annotation.serviceName();
        RpcRequest request = new RpcRequest();
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);
        request.setServiceName(serviceName);
/*        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());*/
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        // 从注册中心获取实例
        List<Instance> instances = ConnectionManager.instance().getInstanceForServiceName(serviceName);
        if(CollectionUtil.isEmptyCollection(instances)){
            throw new RpcInvokeException(String.format("从注册中心获取服务 %s 的实例失败",serviceName));
        }
        // 负载均衡选择调用的示例
        Instance instance = loadBlance.chooseHandler(instances);
        String address = instance.getAddress();
        // 根据实例的地址获取Transporter
        Transporter transporter = ConnectionManager.instance().getOrCreateTransporter(address);
        int retry = 1;
        // 获取不到handler，则重试
        while (transporter == null){
            // 使用随机负载均衡算法
            instance = LoadBlance.RANDOM.chooseHandler(instances);
            transporter = ConnectionManager.instance().getOrCreateTransporter(instance.getAddress());
            retry++;
            if(retry >= DEFAULT_RETRY_COUNTS){
                break;
            }
        }
        if(transporter == null){
            // 如果始终获取不到有效实例，则异常退出。
            throw new RpcInvokeException("Cant get available server to invoke method :" + method.getName());
        }
        final String asyncReturn = ResponseFuture.class.getTypeName();
        Type returnType = method.getReturnType();
        int timeout = annotation.timeout();

        Object returnValue = null;
        if(asyncReturn.equals(returnType.getTypeName())){
            // 异步执行
           returnValue =  transporter.asyncSend(request);
        } else {
            // 同步执行
            if(timeout <= 0 ){
                // 如果timeout设置出错，那么使用config中提供的超时时间
                returnValue = transporter.syncSend(request);
            } else {
                // 否则使用注解的超时时间。
                returnValue = transporter.syncSend(request, timeout);
            }
        }
        return returnValue;
    }
}
