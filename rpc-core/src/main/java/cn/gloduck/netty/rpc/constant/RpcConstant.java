package cn.gloduck.netty.rpc.constant;

public class RpcConstant {

    public static final byte[] MAGIC_NUMBER = {0,5,1,2,5,3,5,1};
    public static final String NAMESPACE = "netty_rpc";
    public static final int MAX_FRAME_LENGTH = 1 << 16;
    public static final long BEAT_READER_IDLE_TIME =0;
    public static final long BEAT_WRITER_IDLE_TIME = 0;
    /**
     * 心跳时间
     */
    public static final long BEAT_ALL_IDLE_TIME = 2 * 30;

}
