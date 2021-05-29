package cn.gloduck.netty.rpc.utils;

public class RuntimeUtil {
    private RuntimeUtil(){}
    public static int availableProcessors(){
        return Runtime.getRuntime().availableProcessors();
    }
}
