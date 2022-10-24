package cn.crisp.filesystem.entity;

import cn.crisp.filesystem.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class Inode implements Serializable {
    public static final long serialVersionUID = 2L;
    private int id; //4 byte 作为在数据块中的标识
    private String name; //30 byte
    private int isDir; //4 byte 0表示文件，1表示目录
    private LocalDateTime createTime; //4 byte 创建时间
    private int length; //4 byte 文件长度，目录文件则记录子文件数量
    private int[] address; // 44 byte 前十项为直接索引，最后一项为间接索引
    private String createBy; // 30 byte 创建者
    private String limit; // 9 byte 权限

    public Inode() {
        id = 0;
        name = "";
        isDir = 0;
        length = 0;
        address = new int[11];
        createTime = LocalDateTime.now();
        createBy = "";
        limit = "rwxrw-r--";
        for (int i = 0; i < 11; i++) {
            address[i] = -1;
        }
    }
}
