package cn.crisp.filesystem.common;

import cn.crisp.filesystem.entity.Inode;
import cn.crisp.filesystem.entity.SuperBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final int TotalSize = 104857600; //100M 100*1024*1024
    public static final int BlockTotalSize = 83886080;
    public static final int KeepTotalSize = 20971520;
    public static final int InodeNum = 131072; //inode数量,2^17
    public static final int TotalBlockNum = 102400; //总的块数量
    public static final int BlockNum = 81920; //可用块数量 80*1024, 剩下的分给启动快、超级块、i结点以及fcb
    public static final int KeepBlockNum = 20480;
    public static final int BlockSize = 1024; //块大小为1k
    public static final int SuperBlockSize = 213004;
    public static final int InodeSize = 90;
    public static final int FcbSize = 42;
    public static final int UserSize = 60;
    public static final int BlockPerNode = 11; //每个文件最大块数限制，前10块为直接索引，最后一块为间接索引
    public static final String DISK = "DISK.txt";


    public static final int BootBlockPos = 0; // 启动块分2个磁盘块，offset为0
    public static final int SuperBlockPos = BootBlockPos + 2 * BlockSize; //超级块offset, 213004 byte分配209个块
    public static final int InodePos = SuperBlockPos + 209 * BlockSize; // I结点的offset, 分配的百分之20磁盘块剩下的分给I结点
    public static final int BlockPos = (TotalBlockNum - BlockNum) * BlockSize; //可用磁盘块的offset


    public static final List<String> CMDList = new ArrayList<>(Arrays.asList("info", "cd", "dir", "md", "rd", "newfile", "cat", "copy", "del", "check"));


}
