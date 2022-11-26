package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

import static cn.crisp.filesystem.common.Constants.BlockNum;

/**
 * 磁盘块位置图同i结点位示图相同，主要通过一个boolean数组来存储磁盘块的使用信息，false代表未被使用，true代表已经被使用，数组长度为可用磁盘块的个数，即81920个。
 */
@AllArgsConstructor
@Data
public class BlockMap implements Serializable {
    public static final long serialVersionUID = 5L;
    private boolean[] blockMap = new boolean[BlockNum];

    public BlockMap(){
        for (int i = 0; i < BlockNum; i++) {
            blockMap[i] = false;
        }
    }
}
