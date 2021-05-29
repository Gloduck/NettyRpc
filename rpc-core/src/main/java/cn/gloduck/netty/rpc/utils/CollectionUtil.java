package cn.gloduck.netty.rpc.utils;

import java.util.Collection;

public class CollectionUtil {
    private CollectionUtil(){}
    public static <T extends Collection> boolean isEmptyCollection(T collection){
        return collection == null || collection.size() == 0;
    }

}
