package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class User implements Serializable {
    public static final long serialVersionUID = 3L;
    private String username;
    private String password;
    private String group;
}
