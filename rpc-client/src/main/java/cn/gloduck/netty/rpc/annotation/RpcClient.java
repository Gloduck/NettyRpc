package cn.gloduck.netty.rpc.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * 标明当前类是一个RPC客户端
 * @author gloduck
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
@Service
public @interface RpcClient {
}
