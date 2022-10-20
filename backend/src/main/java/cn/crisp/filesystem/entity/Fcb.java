package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class Fcb implements Serializable {
    public static final long serialVersionUID = 3L;
    private int id;
    private String name;
    private int isDir;
    private int blockId;

    public Fcb() {
        id = 0;
        name = "";
        isDir = 0;
        blockId = 0;
    }
}
