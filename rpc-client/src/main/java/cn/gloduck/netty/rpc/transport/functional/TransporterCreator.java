package cn.gloduck.netty.rpc.transport.functional;

import cn.gloduck.netty.rpc.transport.client.Transporter;

/**
 *
 * @author Gloduck
 */
public interface TransporterCreator {
    /**
     * 创建一个新的连接
     * @return
     */
    Transporter createTransporter(String host, int port);
}
