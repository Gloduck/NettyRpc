package cn.gloduck.netty.rpc.registry;

import cn.gloduck.netty.rpc.constant.RpcConstant;
import cn.gloduck.netty.rpc.utils.RuntimeUtil;

public class RegistryConfig {
    /**
     * 默认的注册中心超时时间
     */
    private static final int TIMEOUT = 5000;
    /**
     * 默认注册中心连接超时时间
     */
    private static final int CONNECTION_TIMEOUT = 5000;

    /**
     * 默认重试次数
     */
    private static final int DEFAULT_RETRY_TIMES = 10;

/*
    public static final long REGISTRY_HEARTBEAT_PERIOD = ;
    public static final long REGISTRY_RECONNECT_PERIOD;
*/


    private RegistryConfig() {
    }

    /**
     * zookeeper命名空间
     */
    private String namespace = RpcConstant.NAMESPACE;
    /**
     * 超时时间
     */
    private int timeout = TIMEOUT;
    /**
     * 连接超时时间
     */
    private int connectionTimeout = CONNECTION_TIMEOUT;

    private int retryIfLoseConnection = DEFAULT_RETRY_TIMES;
    /**
     * 地址列表
     */
    private String address;

    /**
     * 是否是临时节点
     * 如果使用临时节点：那么断开连接的时候，将zookeeper将自动消失。好处是如果服务端异常关闭，也不会有垃圾数据。<br>
     * 坏处是如果和zookeeper的网络闪断也通知客户端，客户端以为是服务端下线<br>
     * 如果使用永久节点：好处：网络闪断时不会影响服务端，而是由客户端进行自己判断长连接<br>
     * 坏处：服务端如果是异常关闭（无反注册），那么数据里就由垃圾节点，得由另外的哨兵程序进行判断
     */
    private boolean isEphemeralNode = true;


    public static Builder builder() {
        RegistryConfig config = new RegistryConfig();
        return new Builder(config);
    }

    public static class Builder {
        private final RegistryConfig config;
        private final StringBuilder addressBuilder;

        private Builder(RegistryConfig config) {
            addressBuilder = new StringBuilder();
            this.config = config;
        }

        public Builder namespace(String namespace) {
            config.namespace = namespace;
            return this;
        }

        public Builder timeout(int timeout) {
            config.timeout = timeout;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            config.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder retryTimes(int nTimes){
            config.retryIfLoseConnection = nTimes;
            return this;
        }

        public Builder addresses(String addresses) {
            addressBuilder.append(",")
                    .append(addresses);
            return this;
        }

        public Builder address(String address) {
            addressBuilder.append(",")
                    .append(address);
            return this;
        }

        public Builder address(String host, int port) {
            addressBuilder.append(",")
                    .append(host)
                    .append(":")
                    .append(port);
            return this;
        }

        public Builder ephemeral(boolean isEphemeral) {
            config.isEphemeralNode = isEphemeral;
            return this;
        }


        public RegistryConfig build() {
            addressBuilder.deleteCharAt(0);
            String address = addressBuilder.toString();
            if ("".equals(address)) {
                throw new IllegalArgumentException("注册中心地址不能为空");
            }
            config.address = address;
            return this.config;
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public String getAddress() {
        return address;
    }

    public boolean isEphemeralNode() {
        return isEphemeralNode;
    }

    public int getRetryIfLoseConnection() {
        return retryIfLoseConnection;
    }

    @Override
    public String toString() {
        return "RegistryConfig{" +
                "namespace='" + namespace + '\'' +
                ", timeout=" + timeout +
                ", connectionTimeout=" + connectionTimeout +
                ", address='" + address + '\'' +
                '}';
    }
}
