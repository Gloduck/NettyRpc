package cn.gloduck.netty.rpc.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class User {
//    private final static long serialVersionUID = 1;
    private Integer id;
    private String name;
    private String password;

}
