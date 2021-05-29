package cn.gloduck.netty.rpc.utils;

public class NumberUtil {
    private NumberUtil(){}

    /**
     * int转字节数组
     * @param target
     * @return
     */
    public static byte[] intToByteArray(int target){
        byte[] b = new byte[4];
        b[0] = (byte) (target & 0xff);
        b[1] = (byte) (target >> 8 & 0xff);
        b[2] = (byte) (target >> 16 & 0xff);
        b[3] = (byte) (target >> 24 & 0xff);
        return b;
    }

    /**
     * 字节数组转int
     * @param array
     * @return
     */
    public static int byteArrayToInt(byte[] array){
        int res = 0;
        for(int i=0;i<array.length;i++){
            res += (array[i] & 0xff) << (i*8);
        }
        return res;
    }

}
