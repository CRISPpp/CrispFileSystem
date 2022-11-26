package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

import static cn.crisp.filesystem.common.Constants.BlockNum;
import static cn.crisp.filesystem.common.Constants.BlockTotalSize;

/**
 * inodeMap：i结点位示图。
 * 	blockMap：磁盘块位示图。
 * 	inodeNum：i结点个数。
 * 	blockNum：已用磁盘块个数。
 * 	blockFree：可用磁盘块个数，初始化为81920。
 * 	lastBlockSize：可用的字节大小，初始化为83886080。
 */
@AllArgsConstructor
@Data
public class SuperBlock implements Serializable {
    public static final long serialVersionUID = 1L;
    private InodeMap inodeMap = new InodeMap(); //2^17 * 1 byte
    private BlockMap blockMap = new BlockMap(); //1024 * 80 * 1 byte
    private int inodeNum; // 4 byte
    private int blockNum; // 4 byte
    private int blockFree; //4 byte
    private int lastBlockSize; // 4 byte
    public SuperBlock(){
        inodeNum = 0;
        blockNum = 0;
        blockFree = BlockNum - blockNum;
        lastBlockSize = BlockTotalSize;
    }
}
