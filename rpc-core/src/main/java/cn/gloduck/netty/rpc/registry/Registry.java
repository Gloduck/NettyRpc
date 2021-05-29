package cn.gloduck.netty.rpc.registry;


import cn.gloduck.netty.rpc.ref.client.ServiceInstance;
import cn.gloduck.netty.rpc.ref.server.ServiceInfo;
import cn.gloduck.netty.rpc.utils.RuntimeUtil;

import java.util.List;

public abstract class Registry {
    /**
     * 注册中心服务配置
     */
    protected RegistryConfig registryConfig;

    /**
     * 注册中心配置
     *
     * @param registryConfig 注册中心配置
     */
    public Registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    /**
     * 启动注册中心
     * @return
     */
    public abstract boolean start();

    /**
     * 初始化注册中心
     */
    public abstract void init();

    /**
     * 关闭注册中心
     * @return
     */
    public abstract boolean stop();


    public  void registrySingle(String host, int port, String serviceName){
        this.registrySingle(host, port, serviceName, RuntimeUtil.availableProcessors());
    }

    /**
     * 注册单个服务
     * @param host
     * @param port
     * @param serviceName
     * @param weight
     */
    public abstract void registrySingle(String host, int port, String serviceName, int weight);

    /**
     * 注册一组服务
     * @param host
     * @param port
     * @param services
     */
    public abstract void registryGroup(String host, int port, List<ServiceInfo> services);

    /**
     * 反注册单个服务
     * @param host
     * @param port
     * @param serviceName
     */
    public abstract void unRegistrySingle(String host, int port, String serviceName);

    /**
     * 注册所有向当前注册中心注册过的服务
     */
    public abstract void registry();

    /**
     * 下线所有当前注册中心注册过的服务
     */
    public abstract void unRegistry();

    /**
     * 服务发现
     * @param serviceName
     * @return
     */
    public abstract ServiceInstance discover(String serviceName);



}
