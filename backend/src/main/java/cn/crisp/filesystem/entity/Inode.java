package cn.crisp.filesystem.entity;

import cn.crisp.filesystem.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;


@Data
@AllArgsConstructor
public class Inode implements Serializable {
    public static final long serialVersionUID = 2L;
    private int id; //4 byte
    private String name; //30 byte
    private int isDir; //4 byte 0表示文件，1表示目录
    private int parent; //4 byte
    private int length; //4 byte 文件长度，目录文件则记录子文件数量
    private int[] address; // 44 byte 前十项为直接索引，最后一项为间接索引
    private int blockId; //4 byte 在目录数据块中的id

    public Inode() {
        id = 0;
        name = "";
        isDir = 0;
        parent = -1;
        length = 0;
        blockId = 0;
        address = new int[11];
        for (int i = 0; i < 11; i++) {
            address[i] = -1;
        }
    }
}
