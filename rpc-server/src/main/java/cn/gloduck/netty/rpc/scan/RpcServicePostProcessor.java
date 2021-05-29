package cn.gloduck.netty.rpc.scan;

import cn.gloduck.netty.rpc.annotation.RpcService;

import cn.gloduck.netty.rpc.ref.server.ServiceInfo;
import cn.gloduck.netty.rpc.registry.Registry;
import cn.gloduck.netty.rpc.transport.functional.BeanMethodRegistration;
import cn.gloduck.netty.rpc.utils.CollectionUtil;
import cn.gloduck.netty.rpc.utils.NetUtil;
import cn.gloduck.netty.rpc.utils.RuntimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 扫描配置了RpcService注解的方法，然后添加到context中
 *
 * @author Gloduck
 */
@Deprecated
//@Component
public class RpcServicePostProcessor implements InstantiationAwareBeanPostProcessor, ApplicationListener<WebServerInitializedEvent>, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(RpcServicePostProcessor.class);
    private final int processors = RuntimeUtil.availableProcessors();
    private ApplicationContext applicationContext;
    private final Registry registry;
    private final BeanMethodRegistration registration;
    /**
     * 某个bean对应了那些rpc方法
     */
    private final Map<String, List<ServiceInfo>> beanRpcServiceMapping;
    /**
     * 某个service对应了那个method对象
     * 注：此处可能是一个不优雅的解决方案，因为serviceName重复的时候可能出错。待修复
     */
    private final Map<String, Method> serviceMethodMapping;
    public RpcServicePostProcessor(Registry registry, BeanMethodRegistration registration) {
        this.registry = registry;
        this.registration = registration;
        this.beanRpcServiceMapping = new HashMap<>(16);
        this.serviceMethodMapping = new HashMap<>(16);
    }

    /**
     * 扫描配置了RpcService的方法，在createBean的时，创建Bean之前会调用InstantiationAwareBeanPostProcessor的postProcessBeforeInstantiation方法。
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        Method[] methods = beanClass.getMethods();
        List<ServiceInfo> serviceInfos = new ArrayList<>();
        String name;
        int weight;
        for (Method method : methods) {
            RpcService annotation = method.getAnnotation(RpcService.class);
            if (annotation != null) {
                name = annotation.serviceName();
                weight = annotation.weight();
                if (weight == 0) {
                    weight = processors;
                }
                // 生成一个serviceInfo对象，用于后期的注册。
                ServiceInfo serviceInfo = new ServiceInfo(name, weight);
                serviceInfos.add(serviceInfo);
                // 将serviceName和method对象绑定，用于后期注册。
                serviceMethodMapping.put(name, method);

                logger.info("Registry method {} to the container, method name is {}", name, method.getName());
            }
        }
        if(!CollectionUtil.isEmptyCollection(serviceInfos)){
            // 如果当前的bean有rpc方法，则添加到map中
            beanRpcServiceMapping.put(beanName, serviceInfos);
        }
        return null;
    }

    /**
     * 初始化完成时间，将服务注册到注册中心上。在Spring初始化完成过后会调用此处方法。
     * @param event
     */
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        String host = NetUtil.getLocalHost();
        int port = registration.getPort();
        beanRpcServiceMapping.forEach((beanName, serviceInfos) -> {
            // 注册服务
            registry.registryGroup(host, port, serviceInfos);
            // 此时容器中的bean已经初始化完成，applicationContext已经注入。
            Object bean = applicationContext.getBean(beanName);
            for (ServiceInfo serviceInfo : serviceInfos) {
                String serviceName = serviceInfo.getServiceName();
                registration.registryServiceBean(serviceName, bean, serviceMethodMapping.get(serviceName));
            }
        });
    }

    /**
     * 在执行初始化Bean的时候会调用各种aware方法
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
