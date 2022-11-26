package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

import static cn.crisp.filesystem.common.Constants.InodeNum;

/**
 * i结点位置图主要通过一个boolean数组来存储i结点的使用信息，false代表未被使用，true代表已经被使用，数组长度为i结点的个数，即131072个。
 */
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
