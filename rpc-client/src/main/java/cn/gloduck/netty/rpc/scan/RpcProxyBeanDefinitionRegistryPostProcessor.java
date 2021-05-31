package cn.gloduck.netty.rpc.scan;

import cn.gloduck.netty.rpc.annotation.RpcClient;
import cn.hutool.core.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * https://blog.csdn.net/lichuangcsdn/article/details/89694363
 * 此处可以扫描带有RpcClient注解的类，然后通过工厂模式创建一个代理，并且注册到Bean容器中。
 * @author Gloduck
 */
@Component
public class RpcProxyBeanDefinitionRegistryPostProcessor implements  BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * 扫描bean并为带有RpcClient注解的接口创建实现类，然后注入到容器
     * @param registry
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Set<Class<?>> classes = ClassUtil.scanPackage();
        for (Class<?> beanClass : classes) {
            RpcClient annotation = beanClass.getAnnotation(RpcClient.class);
            if(annotation != null){
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
                GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                //在这里，我们可以给该对象的属性注入对应的实例。
                //比如mybatis，就在这里注入了dataSource和sqlSessionFactory，
                // 注意，如果采用definition.getPropertyValues()方式的话，
                // 类似definition.getPropertyValues().add("interfaceType", beanClazz);
                // 则要求在FactoryBean（本应用中即ServiceFactory）提供setter方法，否则会注入失败
                // 如果采用definition.getConstructorArgumentValues()，
                // 则FactoryBean中需要提供包含该属性的构造方法，否则会注入失败
                definition.getConstructorArgumentValues().addGenericArgumentValue(beanClass);

                //注意，这里的BeanClass是生成Bean实例的工厂，不是Bean本身。
                // FactoryBean是一种特殊的Bean，其返回的对象不是指定类的一个实例，
                // 其返回的是该工厂Bean的getObject方法所返回的对象。
                definition.setBeanClass(RpcServiceFactory.class);

                //这里采用的是byType方式注入，类似的还有byName等
                definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                registry.registerBeanDefinition(beanClass.getSimpleName(), definition);

            }
        }

    }
}
