package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

import static cn.crisp.filesystem.common.Constants.InodeNum;

@AllArgsConstructor
@Data
public class InodeMap implements Serializable {
    public static final long serialVersionUID = 4L;
    private boolean[] inodeMap = new boolean[InodeNum];
    public InodeMap(){
        for (int i = 0; i < InodeNum; i++) {
            inodeMap[i] = false;
        }
    }
}
