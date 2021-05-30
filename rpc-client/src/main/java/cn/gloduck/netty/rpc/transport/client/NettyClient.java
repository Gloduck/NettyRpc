package cn.gloduck.netty.rpc.transport.client;

import cn.gloduck.netty.rpc.transport.AbstractNettyServer;
import cn.gloduck.netty.rpc.transport.NettyConfig;
import cn.gloduck.netty.rpc.transport.functional.TransporterCreator;
import cn.gloduck.netty.rpc.utils.NetUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty客户端
 * @author Gloduck
 */
public class NettyClient extends AbstractNettyServer implements TransporterCreator {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private EventLoopGroup eventLoopGroup;
    public NettyClient(NettyConfig nettyConfig) {
        super(nettyConfig);
    }

    @Override
    protected boolean initNettyServer() throws Exception {
        eventLoopGroup = new NioEventLoopGroup(nettyConfig.getWorkerThread());
        return true;
    }

    @Override
    protected void destroyNettyServer() {
        if(eventLoopGroup != null){
            eventLoopGroup.shutdownGracefully();
            eventLoopGroup = null;
        }
    }

    /**
     * 创建一个新的连接
     * @param host
     * @param port
     * @return
     */
    @Override
    public Transporter createTransporter(String host, int port) {
        String remoteAddress = NetUtil.toUrlString(host, port);
        RpcClientInitializer initializer = new RpcClientInitializer(remoteAddress,nettyConfig);
        Bootstrap bootstrap = new Bootstrap();
        // 设置连接超时时间
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyConfig.getConnectTimeout());
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(initializer);
        Channel channel = bootstrap.connect(host, port)
                .syncUninterruptibly()
                .channel();
        RpcResponseHandler responseHandler = initializer.handler();
        Transporter transporter = new Transporter(nettyConfig.getRequestTimeout(), channel, responseHandler);
        return transporter;
    }
}
