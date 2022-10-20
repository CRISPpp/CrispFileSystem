package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

import static cn.crisp.filesystem.common.Constants.BlockNum;

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
