package cn.gloduck;

import cn.gloduck.netty.rpc.codec.RpcDecoder;
import cn.gloduck.netty.rpc.codec.RpcEncoder;
import cn.gloduck.netty.rpc.codec.RpcRequest;
import cn.gloduck.netty.rpc.constant.RpcConstant;
import cn.gloduck.netty.rpc.serializer.jdk.JdKSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class Bootstraps {
    private static final int MAX_FRAME_LENGTH = 1 << 16;
    public static void startServer(int port){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            // 设置接收缓冲区10字节
            b.option(ChannelOption.SO_RCVBUF,10);
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new RpcServiceCodecChain());
//                    .childHandler(new RpcMessageHandler());

            Channel ch = b.bind(port).sync().channel();
            System.out.println("服务器启动成功");
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    public static class RpcServiceCodecChain extends ChannelInitializer<SocketChannel>{
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, RpcConstant.MAGIC_NUMBER.length + 2, 4))
                    .addLast(new RpcDecoder(new JdKSerializer()))
                    .addLast(new RpcEncoder(new JdKSerializer()));
        }
    }
/*    public static class RpcMessageHandler extends SimpleChannelInboundHandler<RpcMessage>{

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
            System.out.println("获取信息为：" + msg.toString());
        }
    }*/


    public static void startClient(int port){
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientTestHandler());
            // 连接服务器
            ChannelFuture channelFuture = bootstrap.connect("localhost",port).sync();

            Channel channel = channelFuture.channel();
                RpcRequest request = new RpcRequest();

                channel.writeAndFlush(request);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
    public static class ClientTestHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new RpcEncoder(new JdKSerializer()));
        }
    }
}
