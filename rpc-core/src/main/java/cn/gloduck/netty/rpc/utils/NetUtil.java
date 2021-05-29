package cn.gloduck.netty.rpc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetUtil {

    private static final Logger logger = LoggerFactory.getLogger(NetUtil.class);
    /**
     * IPv4正则
     */
    private final static Pattern IPV4 = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
    /**
     * IPv6正则
     */
    private final static Pattern IPV6 = Pattern.compile("(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))");

    /**
     * url分隔符
     */
    private static final char URL_SEP = '/';
    private NetUtil(){}
    public static String toUrlString(String host, int port){
        return String.format("%s:%d", host, port);
    }

    

    public static String buildPath(String rootPath, String... paths){
        StringBuilder builder = new StringBuilder(rootPath);
        if(builder.charAt(0) != URL_SEP){
            builder.insert(0, URL_SEP);
        }
        int length = builder.length();
        if(builder.charAt(length - 1) == URL_SEP){
            builder.deleteCharAt(length - 1);
        }
        for (String path : paths) {
            builder.append("/")
                    .append(path);
        }
        return builder.toString();
    }

    public static String getLocalHost(){
        String host = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            host =  localHost.getHostAddress();
        } catch (UnknownHostException e){
            logger.error("获取host地址失败");
        }
        return host;
    }

    public static long ipv4ToLong(String ipAddress){
        if(isIpv4(ipAddress)){
            long[] ip = new long[4];
            // 先找到IP地址字符串中.的位置
            int position1 = ipAddress.indexOf(".");
            int position2 = ipAddress.indexOf(".", position1 + 1);
            int position3 = ipAddress.indexOf(".", position2 + 1);
            // 将每个.之间的字符串转换成整型
            ip[0] = Long.parseLong(ipAddress.substring(0, position1));
            ip[1] = Long.parseLong(ipAddress.substring(position1 + 1, position2));
            ip[2] = Long.parseLong(ipAddress.substring(position2 + 1, position3));
            ip[3] = Long.parseLong(ipAddress.substring(position3 + 1));
            return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
        } else {
            return 0;
        }
    }

    public static boolean isIpv4(String ipAddress){
        Matcher matcher = IPV4.matcher(ipAddress);
        return matcher.matches();
    }
}
