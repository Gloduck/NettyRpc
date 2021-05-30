package cn.gloduck.netty.rpc.service;

import cn.gloduck.netty.rpc.annotation.RpcClient;
import cn.gloduck.netty.rpc.annotation.RpcReference;
import cn.gloduck.netty.rpc.entity.User;
import cn.gloduck.netty.rpc.loadbance.LoadBlance;
import cn.gloduck.netty.rpc.transport.client.ResponseFuture;
import org.springframework.stereotype.Service;

@RpcClient
public interface TestService {

    @RpcReference(serviceName = "getUser",loadBlance = LoadBlance.IP_HASH)
    User getUser(Integer id, String name, String password);

    @RpcReference(serviceName = "updateUser", loadBlance = LoadBlance.RANDOM)
    int updateUser(User user);

    @RpcReference(serviceName = "async",loadBlance = LoadBlance.RANDOM)
    ResponseFuture<String> async();
}
