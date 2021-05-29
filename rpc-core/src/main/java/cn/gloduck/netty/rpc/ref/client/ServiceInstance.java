package cn.gloduck.netty.rpc.ref.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceInstance {
    private String name;
    private List<Instance> hosts = new CopyOnWriteArrayList<>();
    public ServiceInstance(String name){
        this.name = name;
        this.hosts = new CopyOnWriteArrayList<>();
    }

    public ServiceInstance(String name, List<Instance> hosts) {
        this.name = name;
        this.hosts = hosts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Instance> getHosts() {
        return hosts;
    }

    public void setHosts(List<Instance> hosts) {
        this.hosts = hosts;
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "name='" + name + '\'' +
                ", hosts=" + hosts +
                '}';
    }

    /**
     * 由于需要作为hashmap的键，所以必须重写equals
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceInstance)) {
            return false;
        }
        ServiceInstance instance = (ServiceInstance) o;
        return name.equals(instance.name);
    }

    /**
     * 由于需要作为hashmap的键，所以必须重写hashcode
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
