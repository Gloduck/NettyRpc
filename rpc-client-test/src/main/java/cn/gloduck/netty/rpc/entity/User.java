package cn.gloduck.netty.rpc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User  {
//    private final static long serialVersionUID = 1;

    private Integer id;
    private String name;
    private String password;

}
