package cn.gloduck.netty.rpc.ref.client;

import cn.gloduck.netty.rpc.utils.NetUtil;

public class Instance {
    private String host;
    private int port;
    private String serviceName;
    private int weight;
    private String address;

    public Instance(String host, int port, String serviceName, int weight) {
        this.address = host + ":" + port;
        this.host = host;
        this.port  =port;
        this.serviceName = serviceName;
        this.weight = weight;
    }
    public Instance(String address, String serviceName, int weight) {
        this.address = address;
        String[] split = address.split(":");
        this.host = split[0];
        this.port = Integer.parseInt(split[1]);
        this.serviceName = serviceName;
        this.weight = weight;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", serviceName='" + serviceName + '\'' +
                ", weight=" + weight +
                '}';
    }
}
