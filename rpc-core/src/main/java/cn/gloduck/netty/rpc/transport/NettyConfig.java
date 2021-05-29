package cn.gloduck.netty.rpc.transport;


import cn.gloduck.netty.rpc.serializer.RpcSerializer;
import cn.gloduck.netty.rpc.serializer.SerializerFactory;
import cn.gloduck.netty.rpc.serializer.jdk.JdKSerializer;
import cn.gloduck.netty.rpc.utils.NetUtil;
import cn.gloduck.netty.rpc.utils.RuntimeUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ReflectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Netty服务器配置文件
 */
public class NettyConfig {
    private static final Logger logger = LoggerFactory.getLogger(NettyConfig.class);
    private static final int DEFAULT_WORKER_THREAD = 4;
    private NettyConfig() {
    }

    String host;
    int port;
    int workerThread;
    int requestTimeout;
    private SerializerFactory<RpcSerializer> serializerFactory;
    ThreadPoolExecutor threadPool;

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

        @Override
        protected void checkAndSetDefaultValue() {
            if(nettyConfig.requestTimeout <= 0){
                nettyConfig.requestTimeout = 500;
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
}
