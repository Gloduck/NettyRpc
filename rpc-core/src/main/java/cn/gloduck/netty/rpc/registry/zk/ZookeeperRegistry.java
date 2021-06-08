package cn.gloduck.netty.rpc.registry.zk;

import cn.gloduck.netty.rpc.exception.RpcException;
import cn.gloduck.netty.rpc.ref.client.Instance;
import cn.gloduck.netty.rpc.ref.client.ServiceInstance;
import cn.gloduck.netty.rpc.ref.server.ServiceInfo;
import cn.gloduck.netty.rpc.registry.Registry;
import cn.gloduck.netty.rpc.registry.RegistryConfig;
import cn.gloduck.netty.rpc.utils.CollectionUtil;
import cn.gloduck.netty.rpc.utils.NetUtil;
import cn.gloduck.netty.rpc.utils.NumberUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 结构大致参考sofa
 * <pre>
 *  在zookeeper上存放的数据结构为：
 *  -$rootPath (根路径)
 *         └--netty_rpc
 *             |--cn.gloduck.rpc.example.Service1 （服务）
 *             |       |-providers （服务提供者列表）
 *             |       |     |--bolt://192.168.1.100:22000?xxx=yyy [weight]
 *             |       |     |--bolt://192.168.1.110:22000?xxx=yyy [weight]
 *             |       |     └--bolt://192.168.1.120?xxx=yyy [weight]
 *             |--cn.gloduck.rpc.example.Service2 （下一个服务）
 *             | ......
 *  </pre>
 *
 * @author Gloduck
 */
public class ZookeeperRegistry extends Registry {
    private final static Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);
    private static final long UPDATE_HOLD_INTERVAL = 5000L;
    private Object nullObject = new Object();
    /**
     * zookeeper客户端
     */
    private CuratorFramework client;

    /**
     * 与注册中心失去联系模式
     */
    private volatile boolean loseConnectionMode = false;

    /**
     * zookeeper后台处理的线程池
     */
//    private ThreadPoolExecutor executor;
    /**
     * 服务名称对应的实例缓存
     */
    private final Map<String, ServiceInstance> serviceCache;

    /**
     * 保存当前应用注册到注册中心的服务
     */
    private Map<String, RegistryServiceInfo> registeredService;


    /**
     * 注册中心配置
     *
     * @param registryConfig 注册中心配置
     */
    public ZookeeperRegistry(RegistryConfig registryConfig) {
        super(registryConfig);
        this.serviceCache = new ConcurrentHashMap<>(16);
        this.registeredService = new ConcurrentHashMap<>(16);
/*        int updateThread = registryConfig.getBackgroundUpdateThread();
        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>(32);
        ThreadFactory threadFactory = new NamedThreadFactory("Registry_update_thread_", false);
        this.executor = new ThreadPoolExecutor(updateThread, updateThread, 0, TimeUnit.MILLISECONDS, queue, threadFactory, new ThreadPoolExecutor.AbortPolicy());*/
    }

    @Override
    public boolean start() {
        if (client == null) {
            logger.warn("Zookeeper还未进行初始化");
            return false;
        }
        if (client.getState() == CuratorFrameworkState.STARTED) {
            return true;
        }
        client.start();
        try {
            addListener(NetUtil.buildPath(registryConfig.getNamespace()), (client1, event) -> {
                // 添加监听器，监听zookeeper的节点变化
                switch (event.getType()) {
                    case CONNECTION_RECONNECTED: // zookeeper重连后
                        this.loseConnectionMode = false;
                        logger.warn("zookeeper重连");
                        break;
                    case CONNECTION_SUSPENDED: // zookeeper挂了后
                        this.loseConnectionMode = true;
                        logger.warn("zookeeper失去连接");
                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            logger.error("添加zookeeper监听器失败，可能无法感知zookeeper的短线");
        }
        return client.getState() == CuratorFrameworkState.STARTED;

    }

    /**
     * 初始化
     */
    @Override
    public synchronized void init() {
        if (this.client != null) {
            return;
        }
        this.client = CuratorFrameworkFactory.builder()
                .namespace(registryConfig.getNamespace())
                .connectionTimeoutMs(registryConfig.getConnectionTimeout())
                .sessionTimeoutMs(registryConfig.getTimeout())
                .connectString(registryConfig.getAddress())
                .retryPolicy(new ExponentialBackoffRetry(1000, registryConfig.getRetryIfLoseConnection()))
                .build();
    }

    public void doDestroy() {
        if (!registryConfig.isEphemeralNode()) {
            // 如果节点是非临时节点，则主动下线服务
            unRegistry();
        }
    }

    @Override
    public boolean stop() {
        doDestroy();
        client.close();
        return true;
    }

    /**
     * 注册单个节点到zookeeper
     *
     * @param host
     * @param port
     * @param serviceName
     * @param weight
     */
    @Override
    public void registrySingle(String host, int port, String serviceName, int weight) {
        boolean flag = false;
        try {
            CuratorFramework zkClient = getAndCheckZkClient();
            try {
                String path = getPath(serviceName, host, port);
                // 获取节点状态
                Stat stat = zkClient.checkExists().forPath(path);
                if (stat != null) {
                    // 如果节点状态不为null，则证明当前节点在zookeeper中存在
                    long ephemeralOwner = stat.getEphemeralOwner();
                    if (ephemeralOwner != 0) {
                        // 如果不为0，则代表当前节点为临时节点。由于路径存在，则大概率是因为当前服务非正常关闭，但是由于还没过timeout，zookeeper认为是服务器断线了，还没删除节点信息
                        // 则手动删除节点信息，然后执行注册。
                        zkClient.delete().deletingChildrenIfNeeded().forPath(path);
                        logger.info("服务器非正常退出，从zookeeper中删除临时节点：{}",path);
                    }
                }
                // 节点不存在，或节点存在，但是是永久节点。则尝试直接添加节点。
                zkClient.create().creatingParentContainersIfNeeded()
                        .withMode(registryConfig.isEphemeralNode() ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT)
                        .forPath(path, NumberUtil.intToByteArray(weight));
                flag = true;

            } catch (KeeperException.NodeExistsException nodeExistsException) {
                // 此时异常，则证明在zookeeper中注册了一个持久节点，则发出提示。
                logger.warn("注册服务:{}到注册中心失败,当前节点已经存在，尝试判断节点状态", serviceName);
            }
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
        // 插入一条注册记录
        if (flag) {
            logger.info("注册服务：{}到zookeeper成功", serviceName);
            RegistryServiceInfo registryServiceInfo = new RegistryServiceInfo(host, port, serviceName, weight);
            registeredService.put(serviceName, registryServiceInfo);
        }

    }

    @Override
    public void registryGroup(String host, int port, List<ServiceInfo> services) {
        for (ServiceInfo service : services) {
            registrySingle(host, port, service.getServiceName(), service.getWeight());
        }
    }

    /**
     * 将单个节点从zookeeper删除
     *
     * @param host
     * @param port
     * @param serviceName
     */
    @Override
    public void unRegistrySingle(String host, int port, String serviceName) {
        boolean flag = false;
        try {
            getAndCheckZkClient().delete()
                    .forPath(getPath(serviceName, host, port));
            flag = true;
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
        if (flag) {
            logger.info("从zookeeper下线服务:{}成功", serviceName);
            // 删除一条注册记录
            this.registeredService.remove(serviceName);
        }
    }

    @Override
    public void registry() {
        if (!registeredService.isEmpty()) {
            CuratorFramework client = getAndCheckZkClient();
            try {
                Set<Map.Entry<String, RegistryServiceInfo>> entrySet = registeredService.entrySet();
                for (Map.Entry<String, RegistryServiceInfo> stringRegistryServiceInfoEntry : entrySet) {
                    RegistryServiceInfo serviceInfo = stringRegistryServiceInfoEntry.getValue();
                    try {
                        client.create()
                                .creatingParentContainersIfNeeded()
                                .withMode(registryConfig.isEphemeralNode() ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT)
                                .forPath(getPath(serviceInfo.serviceName, serviceInfo.host, serviceInfo.port), NumberUtil.intToByteArray(serviceInfo.weight));
                    } catch (KeeperException.NodeExistsException e) {
                        logger.warn("注册服务:{}到注册中心失败,当前节点已经存在", serviceInfo.serviceName);
                    }
                }
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public void unRegistry() {
        if (!registeredService.isEmpty()) {
            CuratorFramework client = getAndCheckZkClient();
            try {
                Set<Map.Entry<String, RegistryServiceInfo>> entrySet = registeredService.entrySet();
                for (Map.Entry<String, RegistryServiceInfo> stringRegistryServiceInfoEntry : entrySet) {
                    RegistryServiceInfo serviceInfo = stringRegistryServiceInfoEntry.getValue();
                    try {
                        client.delete()
                                .forPath(getPath(serviceInfo.serviceName, serviceInfo.host, serviceInfo.port));
                    } catch (KeeperException.NoNodeException e) {
                        logger.warn("当前实例在注册中心上不存在 : {}", serviceInfo.serviceName);
                    }
                }
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
        }
    }

    /**
     * 服务发现
     *
     * @param serviceName
     * @return
     */
    @Override
    public ServiceInstance discover(String serviceName) {
        if (loseConnectionMode) {
            logger.warn("与注册中心失去连接，从缓存中获取");
            // 与注册中心失去连接，直接获取
            return serviceCache.getOrDefault(serviceName, new ServiceInstance(serviceName, Collections.emptyList()));
        }
        ServiceInstance serviceInstance = serviceCache.get(serviceName);
        if (serviceInstance == null) {
            serviceInstance = serviceCache.get(serviceName);
            if (serviceInstance == null) {
                List<Instance> instances = doDiscover(serviceName);
                serviceInstance = new ServiceInstance(serviceName);
                processServiceInstance(serviceInstance, instances);
                serviceCache.put(serviceInstance.getName(), serviceInstance);

            }
        }
        return serviceCache.get(serviceName);
    }


    /**
     * 从zookeeper中获取数据
     * 注：待更改
     *
     * @param serviceName
     * @return
     */
    protected List<Instance> doDiscover(String serviceName) {
        logger.info("从zookeeper中获取服务 {} 的实例", serviceName);
        List<Instance> res = new CopyOnWriteArrayList<>();
        try {
            try {
                String path = NetUtil.buildPath(registryConfig.getNamespace(), serviceName);
                // 添加节点监听器
                addListener(path, (client1, event) -> {
                    logger.debug("收到zookeeper事件，{}", event.getType());
                    ChildData data = event.getData();
                    if (data == null) {
                        return;
                    }
                    String dataPath = data.getPath();
                    if (dataPath == null) {
                        return;
                    }
                    String address = getAddressFromChildrenPath(dataPath);
                    if (address == null) {
                        logger.error("服务：{} 的提供者发生变化，但是解析其地址失败，解析失败的地址为：{}", serviceName, dataPath);
                        return;
                    }
                    switch (event.getType()) {
                        case CHILD_ADDED: // 添加了一个提供者
                            int weight = NumberUtil.byteArrayToInt(data.getData());
                            handleAdd(serviceName, address, weight);
                            break;
                        case CHILD_REMOVED: // 删除了一个提供者
                            handleDelete(serviceName, address);
                            break;
                        case CHILD_UPDATED: // 更新了一个提供者
                            weight = NumberUtil.byteArrayToInt(data.getData());
                            handleUpdate(serviceName, address, weight);
                            break;
                        default:
                            break;

                    }
                });
                CuratorFramework client = getAndCheckZkClient();
                // 获取当前服务的提供者address
                List<String> list = client.getChildren()
                        .forPath(path);
                if (!CollectionUtil.isEmptyCollection(list)) {
                    // 获取每个提供者的权重
                    res = list.stream().map(address -> {
                        String dataPath = NetUtil.buildPath(registryConfig.getNamespace(), serviceName, address);
                        byte[] data = null;
                        try {
                            data = client.getData()
                                    .forPath(dataPath);
                        } catch (Exception e) {
                            logger.warn("获取实例：{} 的权重失败", dataPath);
                        }
                        if (data != null) {
                            Instance instance = new Instance(address, serviceName, NumberUtil.byteArrayToInt(data));
                            return instance;
                        } else {
                            return null;
                        }
                    }).collect(Collectors.toList());
                }
            } catch (KeeperException.NoNodeException e) {
                logger.warn("当前服务没有实例 : {}", serviceName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return res;
    }


    /**
     * 处理服务实例
     * 注：待更改，可能有线程安全问题，同时需要移除
     *
     * @param serviceInstance
     * @param instances
     */
    protected void processServiceInstance(ServiceInstance serviceInstance, List<Instance> instances) {
        List<Instance> hosts = serviceInstance.getHosts();
        if (CollectionUtil.isEmptyCollection(hosts)) {
            serviceInstance.setHosts(instances);
        } else {
            int oldSize = hosts.size();
            int cap = (int) (oldSize / 0.75 + 1);
            Set<String> set = new HashSet<>(cap);
            hosts.forEach(instance -> {
                set.add(instance.getAddress());
            });
            instances.forEach(instance -> {
                if (!set.contains(instance.getAddress())) {
                    // 如果当前不存在
                    hosts.add(instance);
                }
            });
        }

    }

    /**
     * 验证并获取zookeeper客户端
     *
     * @return
     */
    private CuratorFramework getAndCheckZkClient() {
        if (client == null || client.getState() != CuratorFrameworkState.STARTED) {
            throw new RpcException("zookeeper未被初始化或非启动状态");
        }
        return client;
    }

    /**
     * 处理zookeeper添加事件
     *
     * @param serviceName
     * @param address
     * @param weight
     */
    private void handleAdd(String serviceName, String address, int weight) {
        boolean flag = false;
        try {
            ServiceInstance serviceInstance = serviceCache.get(serviceName);
            Instance instance = null;
            if (serviceInstance != null) {
                // 多加一次判断
                List<Instance> hosts = serviceInstance.getHosts();
                instance = new Instance(address, serviceName, weight);
                hosts.add(instance);
                flag = true;
            }
            if (flag) {
                logger.info("服务：{}，添加了一个提供者，地址为：{}，数据为：{}", serviceName, address, instance.toString());
            } else {
                logger.warn("添加服务提供者失败");
            }
        } catch (Exception e) {
            logger.error("添加服务提供者失败", e);
        }
    }

    /**
     * 处理zookeeper删除事件
     *
     * @param serviceName
     * @param address
     */
    private void handleDelete(String serviceName, String address) {
        boolean flag = false;
        try {
            ServiceInstance serviceInstance = serviceCache.get(serviceName);
            if (serviceInstance != null) {
                List<Instance> hosts = serviceInstance.getHosts();
                flag = hosts.removeIf(next -> address.equals(next.getAddress()));
            }
            if (!flag) {
                logger.error("删除服务提供者失败");
            } else {
                logger.info("服务：{}，删除了一个提供者，地址为：{}", serviceName, address);
            }
        } catch (Exception e) {
            logger.error("删除服务提供者失败", e);
        }
    }

    /**
     * 处理更新事件
     *
     * @param serviceName
     * @param address
     * @param weight
     */
    private void handleUpdate(String serviceName, String address, int weight) {
        boolean flag = false;
        try {
            ServiceInstance serviceInstance = serviceCache.get(serviceName);
            Instance instance = null;
            if (serviceInstance != null) {
                // 多加一次判断
                List<Instance> hosts = serviceInstance.getHosts();
                for (Instance next : hosts) {
                    if (address.equals(next.getAddress())) {
                        next.setWeight(weight);
                        flag = true;
                        break;
                    }
                }
            }
            if (flag) {
                logger.info("服务：{}，更新了一个提供者，地址为：{}，更新后数据为：{}", serviceName, address, instance.toString());
            } else {
                logger.warn("更新服务提供者失败");
            }
        } catch (Exception e) {
            logger.error("更新服务提供者失败", e);
        }
    }


    /**
     * 添加监听器
     * 监听器介绍：https://blog.csdn.net/sqh201030412/article/details/51446434
     *
     * @param path
     * @param listener
     */
    private void addListener(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        cache.getListenable().addListener(listener);
    }

    /**
     * 获取在zookeeper上的路径
     *
     * @param serviceName
     * @param host
     * @param port
     * @return
     */
    protected String getPath(String serviceName, String host, int port) {
        String s = NetUtil.toUrlString(host, port);
        return NetUtil.buildPath(registryConfig.getNamespace(), serviceName, s);
    }

    /**
     * 从子节点中获取地址
     *
     * @param path
     * @return
     */
    protected String getAddressFromChildrenPath(String path) {
        String res = null;
        try {
            int index = path.lastIndexOf("/") + 1;
            res = path.substring(index);
        } catch (Exception e) {

        }
        return res;
    }

    private static class RegistryServiceInfo {
        private String host;
        private int port;
        private String serviceName;
        private int weight;

        public RegistryServiceInfo(String host, int port, String serviceName, int weight) {
            this.host = host;
            this.port = port;
            this.serviceName = serviceName;
            this.weight = weight;
        }
    }

}
