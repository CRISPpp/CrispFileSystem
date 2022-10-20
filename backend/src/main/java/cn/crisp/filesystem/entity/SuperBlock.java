package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

import static cn.crisp.filesystem.common.Constants.BlockNum;

@AllArgsConstructor
@Data
public class SuperBlock implements Serializable {
    public static final long serialVersionUID = 1L;
    private InodeMap inodeMap = new InodeMap(); //2^17 * 1 byte
    private BlockMap blockMap = new BlockMap(); //1024 * 80 * 1 byte
    private int inodeNum; // 4 byte
    private int blockNum; // 4 byte
    private int blockFree; //4 byte

    public SuperBlock(){
        inodeNum = 0;
        blockNum = 0;
        blockFree = BlockNum - blockNum;
    }
}
