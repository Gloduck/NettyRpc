package cn.gloduck.netty.rpc.ref.server;

import cn.gloduck.netty.rpc.utils.RuntimeUtil;

/**
 * 实例信息
 */
public class ServiceInfo {
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 服务对应的权重，默认为CPU核心数
     */
    private int weight;

    public ServiceInfo(String serviceName, int weight) {
        this.serviceName = serviceName;
        this.weight = weight;
    }

    public ServiceInfo(String serviceName) {
        this.serviceName = serviceName;
        this.weight = RuntimeUtil.availableProcessors();
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getWeight() {
        return weight;
    }
}
