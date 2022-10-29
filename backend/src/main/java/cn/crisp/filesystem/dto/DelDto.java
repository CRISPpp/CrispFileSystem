package cn.crisp.filesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DelDto implements Serializable {
    public static final long serialVersionUID = 19L;
    private String path;
    private String username;
    private String group;
}
