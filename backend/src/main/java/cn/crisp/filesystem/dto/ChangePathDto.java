package cn.crisp.filesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePathDto implements Serializable {
    public static final long serialVersionUID = 11L;
    private String path;
    private String username;
    private String group;
}
