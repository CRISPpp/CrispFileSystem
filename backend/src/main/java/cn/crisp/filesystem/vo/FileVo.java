package cn.crisp.filesystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileVo implements Serializable {
    public static final long serialVersionUID = 11L;
    private String filename;
    private int id;
    private int[] address;
    private String limit;
    private int length;
    private String createBy;
    private LocalDateTime createTime;
    private int isDir;

}
