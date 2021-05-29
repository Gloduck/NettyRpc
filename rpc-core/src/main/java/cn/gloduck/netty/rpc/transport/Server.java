package cn.gloduck.netty.rpc.transport;

/**
 * rpc服务器通用接口
 * @author Gloduck
 */
public  interface  Server {
    void start() throws Exception;

     void stop() throws Exception;

}
