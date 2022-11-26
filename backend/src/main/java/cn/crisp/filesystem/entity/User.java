package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户的主属性主要包括：
 * 	username：用户名
 * 	password：密码
 * 	group：用户组
 * 每个用户都有一个固定的用户组，并且对于各个同组不同用户、不同组不同用户有如下规则：
 * 	用户可对自己的文件进行增、删、查、改。
 * 	对于不同用户，可以进入同组的文件夹，并且可以读取同组的用户的文件，但是不能修改其他同组用户的文件。
 * 	对于不同用户，不可以进入不同组的文件夹，无权限对别的用户的文件夹进行读写。
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
public class User implements Serializable {
    public static final long serialVersionUID = 3L;
    private String username;
    private String password;
    private String group;
}
