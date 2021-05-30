package cn.gloduck.netty.rpc.loadbance;


import cn.gloduck.netty.rpc.ref.client.Instance;
import cn.gloduck.netty.rpc.ref.client.ServiceInstance;
import cn.gloduck.netty.rpc.utils.CollectionUtil;
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
     * 加权随机
     */
    WEIGHT_RANDOM() {
        @Override
        public Instance chooseHandler(List<Instance> instances) {
            // 总权重
            int wightSum = 0;
            for (Instance instance : instances) {
                wightSum += instance.getWeight();
            }
            //获取总权值之间任意一随机数
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int randomWeight = random.nextInt(wightSum);
            //{.},{..},{...},{....}...根据权值概率区间，获得加权随机对象
            for (Instance instance : instances) {
                randomWeight -= instance.getWeight();
                if(randomWeight < 0){
                    return instance;
                }
            }
            return null;
        }
    },
    /**
     * 源地址Hash
     */
    IP_HASH {
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
