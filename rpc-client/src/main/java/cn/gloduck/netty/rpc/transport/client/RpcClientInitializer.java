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
    private NettyConfig nettyConfig;
    private RpcResponseHandler clientHandler;
    private RpcSerializer serializer;

    public RpcClientInitializer(NettyConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
    }
    /*    private final ThreadPoolExecutor executor;

    public RpcClientInitializer(RpcSerializer serializer, ThreadPoolExecutor executor) {
        this.serializer = serializer;
        this.executor = executor;
    }*/

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        clientHandler = new RpcResponseHandler();
        serializer = nettyConfig.getNewSerializer();
        if (serializer == null) {
            // 反射创建序列化器失败，使用默认的JDK序列化器
            logger.warn("创建序列化器失败，使用默认的JDK序列化器");
            serializer = new JdKSerializer();
        }
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(RpcConstant.MAX_FRAME_LENGTH, RpcConstant.MAGIC_NUMBER.length + 2, 4))
                .addLast(new IdleStateHandler(RpcConstant.BEAT_READER_IDLE_TIME, RpcConstant.BEAT_WRITER_IDLE_TIME, RpcConstant.BEAT_ALL_IDLE_TIME, TimeUnit.SECONDS))
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
