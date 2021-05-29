package cn.gloduck.netty.rpc.annotation;

import java.lang.annotation.*;

/**
 * 注解，标明一个接口为rpc的服务
 * @author Gloduck
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
public @interface RpcService {
    /**
     * 服务名称
     * @return
     */
    String serviceName();

    /**
     * 服务权重
     * @return
     */
    int weight() default 0;
}
