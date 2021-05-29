package cn.gloduck.netty.rpc.loadbance;


import cn.gloduck.netty.rpc.ref.client.Instance;
import cn.gloduck.netty.rpc.ref.client.ServiceInstance;
import cn.gloduck.netty.rpc.utils.NetUtil;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum LoadBlance {
    /**
     * 随机
     */
    RANDOM {
        @Override
        public Instance chooseHandler(List<Instance> instances) {

            ThreadLocalRandom random = ThreadLocalRandom.current();
            return instances.get(random.nextInt(instances.size()));
        }
    },
    /**
     * 源地址Hash
     */
    IP_HASH{
        @java.lang.Override
        public Instance chooseHandler(List<Instance> instances) {
            int size = instances.size();
            String localHost = NetUtil.getLocalHost();
            long ipv4ToLong = NetUtil.ipv4ToLong(localHost);
            int index = (int) (ipv4ToLong % size);
            return instances.get(index);
        }
    },
    ;




    public Instance chooseHandler(List<Instance> instances) {
        throw new AbstractMethodError();
    }
}
