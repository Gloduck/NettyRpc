package cn.gloduck.netty.rpc.transport.client;

import cn.gloduck.netty.rpc.codec.RpcDecoder;
import cn.gloduck.netty.rpc.codec.RpcEncoder;
import cn.gloduck.netty.rpc.constant.RpcConstant;
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

import java.util.concurrent.TimeUnit;

/**
 * 初始化器
 *
 * @author Gloduck
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    private Logger logger = LoggerFactory.getLogger(RpcClientInitializer.class);
    /**
     * 连接的远程地址
     */
    private String remoteAddress;
    /**
     * netty配置文件
     */
    private NettyConfig nettyConfig;
    /**
     * initChannel初始化的handler
     */
    private RpcResponseHandler clientHandler;
    /**
     * initChannel初始化的序列化器
     */
    private RpcSerializer serializer;

    public RpcClientInitializer(String remoteAddress, NettyConfig nettyConfig) {
        this.remoteAddress = remoteAddress;
        this.nettyConfig = nettyConfig;
    }
    /*    private final ThreadPoolExecutor executor;

    public RpcClientInitializer(RpcSerializer serializer, ThreadPoolExecutor executor) {
        this.serializer = serializer;
        this.executor = executor;
    }*/

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        clientHandler = new RpcResponseHandler(remoteAddress);
        serializer = nettyConfig.getNewSerializer();
        if (serializer == null) {
            // 反射创建序列化器失败，使用默认的JDK序列化器
            logger.warn("创建序列化器失败，使用默认的JDK序列化器");
            serializer = new JdKSerializer();
        }
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(RpcConstant.MAX_FRAME_LENGTH, RpcConstant.MAGIC_NUMBER.length + 2, 4))
                .addLast(new IdleStateHandler(RpcConstant.BEAT_READER_IDLE_TIME, RpcConstant.BEAT_WRITER_IDLE_TIME, nettyConfig.getHeartBeatInterval(), TimeUnit.SECONDS))
                .addLast(new RpcDecoder(serializer))
                .addLast(new RpcEncoder(serializer))
                .addLast(clientHandler);
    }

    public RpcResponseHandler handler() {
        return this.clientHandler;
    }

    public RpcSerializer serializer() {
        return this.serializer;
    }

}
