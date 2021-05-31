package cn.gloduck.netty.rpc.transport;


import cn.gloduck.netty.rpc.serializer.RpcSerializer;
import cn.gloduck.netty.rpc.serializer.SerializerFactory;
import cn.gloduck.netty.rpc.serializer.jdk.JdKSerializer;
import cn.gloduck.netty.rpc.thread.NamedThreadFactory;
import cn.gloduck.netty.rpc.utils.NetUtil;
import cn.gloduck.netty.rpc.utils.RuntimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Netty服务器配置文件
 */
public class NettyConfig {
    private static final Logger logger = LoggerFactory.getLogger(NettyConfig.class);
    /**
     * Netty默认worker线程。客户端和服务端参数
     */
    private static final int DEFAULT_WORKER_THREAD = 4;
    /**
     * 默认心跳数。服务端参数
     */
    private static final int DEFAULT_HEART_BEAT_TIMES = 10;

    /**
     * 默认接收心跳间隔。服务端参数
     */
    private static final int DEFAULT_HEART_BEAT_INTERVAL = 60;

    /**
     * 默认发送心跳的间隔。客户端参数
     */
    private static final int DEFAULT_HEART_BEAT_SEND_INTERVAL = 60;

    /**
     * 默认请求超时事件，客户端参数
     */
    private static final int DEFAULT_REQUEST_TIMEOUT = 500;

    /**
     * 默认连接服务器超时事件，客户端参数
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 500;
    private NettyConfig() {
    }

    private String host;
    private int port;
    private int workerThread;
    private int requestTimeout;
    private SerializerFactory<RpcSerializer> serializerFactory;
    private ThreadPoolExecutor threadPool;
    private int heartBeatTimes;
    private int heartBeatInterval;
    private int connectTimeout;


    public static ClientConfigBuilder clientBuilder() {
        return new ClientConfigBuilder();
    }

    public static ServerConfigBuilder serverBuilder() {
        return new ServerConfigBuilder();
    }



    private static abstract class AbstractNettyConfigBuilder {
        protected static final int DEFAULT_SERVER_PORT = 8026;
        protected static final int DEFAULT_CLIENT_PORT = 8027;
        protected NettyConfig nettyConfig;

        public AbstractNettyConfigBuilder() {
            this.nettyConfig = new NettyConfig();
        }

        public NettyConfig build() {
            doSetDefaultValue();
            return nettyConfig;
        }

        private final void doSetDefaultValue(){
            if(nettyConfig.serializerFactory == null){
                nettyConfig.serializerFactory = new SerializerFactory<>(defaultSerializer());
            }
            if(nettyConfig.threadPool == null){
                nettyConfig.threadPool = defaultThreadPool();
            }
            if(nettyConfig.workerThread <= 0){
                nettyConfig.workerThread = DEFAULT_WORKER_THREAD;
            }
            checkAndSetDefaultValue();
        }

        /**
         * 默认的线程池
         * @return
         */
        protected abstract ThreadPoolExecutor defaultThreadPool();

        /**
         * 默认的序列化器
         * @return
         */
        protected abstract Class<? extends RpcSerializer> defaultSerializer();

        /**
         * 检查必须的配置并且设置默认值
         */
        protected abstract void checkAndSetDefaultValue();
    }
/*
    public static class NettyClientConfig extends NettyConfig{

    }

    public static class NettyServerConfig*/

    /**
     * 客户端配置文件
     */
    public static class ClientConfigBuilder extends AbstractNettyConfigBuilder {
        private ClientConfigBuilder() {
            super();
        }

        /**
         * 客户端只需要发送请求，所以不需要线程池来单独处理
         * @return
         */
        @Override
        protected ThreadPoolExecutor defaultThreadPool() {
            return null;
        }

        @Override
        protected Class<? extends RpcSerializer> defaultSerializer() {
            return JdKSerializer.class;
        }

        /**
         * netty客户端运行的端口
         *
         * @param port
         * @return
         */
        public ClientConfigBuilder port(int port) {
            nettyConfig.host = NetUtil.getLocalHost();
            nettyConfig.port = port;
            return this;
        }

        /**
         * 指定序列化器
         * @param serializer
         * @return
         */
        public ClientConfigBuilder serializer(Class<? extends RpcSerializer> serializer) {
            nettyConfig.serializerFactory = new SerializerFactory<>(serializer);
            return this;
        }

        /**
         * 指定netty工作线程数
         * @param thread
         * @return
         */
        public ClientConfigBuilder workerThread(int thread){
            nettyConfig.workerThread = thread;
            return this;
        }

        /**
         * 请求超时时间
         * @param timeout
         * @return
         */
        public ClientConfigBuilder requestTimeout(int timeout){
            nettyConfig.requestTimeout = timeout;
            return this;
        }
        /**
         * 连接超时时间
         * @param timeout
         * @return
         */
        public ClientConfigBuilder connectTimeout(int timeout){
            nettyConfig.connectTimeout = timeout;
            return this;
        }
        /**
         * 发送心跳的间隔
         * @param interval
         * @return
         */
        public ClientConfigBuilder heartBeatSendInterval(int interval){
            nettyConfig.heartBeatInterval = interval;
            return this;
        }

        @Override
        protected void checkAndSetDefaultValue() {
            if(nettyConfig.requestTimeout <= 0){
                nettyConfig.requestTimeout = DEFAULT_REQUEST_TIMEOUT;
            }
            if(nettyConfig.heartBeatInterval <= 0){
                nettyConfig.heartBeatInterval = DEFAULT_HEART_BEAT_SEND_INTERVAL;
            }
            if(nettyConfig.connectTimeout <= 0){
                nettyConfig.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
            }

        }

    }

    /**
     * 服务器配置文件
     */
    public static class ServerConfigBuilder extends AbstractNettyConfigBuilder {

        private ServerConfigBuilder() {
            super();
        }

        @Override
        protected ThreadPoolExecutor defaultThreadPool() {
            final String threadPrefix = "netty business thread";
            int availableProcessors = RuntimeUtil.availableProcessors();
            BlockingQueue<Runnable> queue = new SynchronousQueue<>();
            ThreadPoolExecutor executor =
                    new ThreadPoolExecutor(availableProcessors, availableProcessors << 1, 60, TimeUnit.MILLISECONDS, queue, new NamedThreadFactory(threadPrefix,false), new ThreadPoolExecutor.AbortPolicy());
            // 空闲时是否回收核心线程
            executor.allowCoreThreadTimeOut(false);
            return executor;
        }

        @Override
        protected Class<? extends RpcSerializer> defaultSerializer() {
           return JdKSerializer.class;
        }

        /**
         * netty服务器的地址
         *
         * @param host
         * @param port
         * @return
         */
        public ServerConfigBuilder address(String host, int port) {
            nettyConfig.host = host;
            nettyConfig.port = port;
            return this;
        }

        /**
         * 多少次没收到心跳就关闭连接
         * @param times
         * @return
         */
        public ServerConfigBuilder heartBeatTimes(int times){
            nettyConfig.heartBeatTimes = times;
            return this;
        }

        /**
         * 检测心跳的间隔
         * @param interval
         * @return
         */
        public ServerConfigBuilder heartBeatInterval(int interval){
            nettyConfig.heartBeatInterval = interval;
            return this;
        }

        /**
         * 指定序列化器
         * @param serializer
         * @return
         */
        public ServerConfigBuilder serializer(Class<? extends RpcSerializer> serializer) {
            nettyConfig.serializerFactory = new SerializerFactory<>(serializer);
            return this;
        }
        /**
         * 自定义线程池
         * @param threadPool
         * @return
         */
        public ServerConfigBuilder threadPool(ThreadPoolExecutor threadPool){
            nettyConfig.threadPool = threadPool;
            return this;
        }

        /**
         * 指定netty工作线程数
         * @param thread
         * @return
         */
        public ServerConfigBuilder workerThread(int thread){
            nettyConfig.workerThread = thread;
            return this;
        }

        @Override
        protected void checkAndSetDefaultValue() {
            if(nettyConfig.heartBeatTimes <= 0){
                nettyConfig.heartBeatTimes = DEFAULT_HEART_BEAT_TIMES;
            }
            if(nettyConfig.heartBeatInterval <= 0){
                nettyConfig.heartBeatInterval = DEFAULT_HEART_BEAT_INTERVAL;
            }
        }

    }

    public RpcSerializer getNewSerializer() {
        return serializerFactory.newInstance();
    }

    public String getAddress() {
        return NetUtil.toUrlString(host, port);
    }

    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public int getWorkerThread() {
        return workerThread;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public int getHeartBeatTimes() {
        return heartBeatTimes;
    }

    public int getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }
}
