package cn.gloduck.netty.rpc.transport.server;

import cn.gloduck.netty.rpc.codec.RpcDecoder;
import cn.gloduck.netty.rpc.codec.RpcEncoder;
import cn.gloduck.netty.rpc.constant.RpcConstant;
import cn.gloduck.netty.rpc.ref.server.BeanAndMethod;
import cn.gloduck.netty.rpc.serializer.RpcSerializer;
import cn.gloduck.netty.rpc.serializer.jdk.JdKSerializer;
import cn.gloduck.netty.rpc.transport.NettyConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Gloduck
 */
public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerInitializer.class);
    private  RpcSerializer serializer;
    private final Map<String, BeanAndMethod> serviceBeanMapping;
    private  ThreadPoolExecutor executor;
    private final NettyConfig config;

    public RpcServerInitializer(NettyConfig config, Map<String, BeanAndMethod> serviceBeanMapping) {
        this.serviceBeanMapping = serviceBeanMapping;
        this.config = config;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        serializer = config.getNewSerializer();
        if(serializer == null){
            logger.warn("创建序列化器失败，使用默认的JDK序列化器");
            serializer = new JdKSerializer();
        }
        executor = config.getThreadPool();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(RpcConstant.MAX_FRAME_LENGTH, RpcConstant.MAGIC_NUMBER.length + 2, 4))
                .addLast(new IdleStateHandler(RpcConstant.BEAT_READER_IDLE_TIME, RpcConstant.BEAT_WRITER_IDLE_TIME, config.getHeartBeatInterval(), TimeUnit.SECONDS))
                .addLast(new RpcDecoder(serializer))
                .addLast(new RpcEncoder(serializer))
                .addLast(new RpcServerHeartBeatHandler(config.getHeartBeatTimes()))
                .addLast(new RpcServerHandler(serviceBeanMapping, executor));

    }
}
