package cn.crisp.filesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CopyDto implements Serializable {
    public static final long serialVersionUID = 20L;
    private String fromPath;
    private String toPath;
    private String username;
    private String group;
}
