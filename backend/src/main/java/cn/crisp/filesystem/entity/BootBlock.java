package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cn.crisp.filesystem.common.Constants.*;

/**
 * 启动块主要属性有：
 * 	userList：用户列表。
 * 	totalSize：文件系统大小104857600。
 * 	blockTotalSize：可用磁盘块大小83886080。
 * 	keepTotalSize：保留的磁盘块大小20971520。
 * 	totalBlockNum：总磁盘块数102400个。
 * 	bootBlockPos：启动块在硬盘的offset，0。
 * 	superBlockPos：超级块在硬盘的offset，2048。
 * 	inodePos：i结点在硬盘的offset，216064。
 * 	blockPos：磁盘块在硬盘的offset，20971520。
 */
@AllArgsConstructor
@Data
public class BootBlock implements Serializable {
    public static final long serialVersionUID = 6L;
    private List<User> userList;
    private int totalSize;
    private int blockTotalSize;
    private int keepTotalSize;
    private int totalBlockNum;
    private int blockNum;
    private int keepBlockNum;
    private int bootBlockPos;
    private int superBlockPos;
    private int inodePos;
    private int blockPos;
    public BootBlock(){
        userList = new ArrayList<>();
        userList.add(new User("system", "system", "root"));
        userList.add(new User("root", "root", "root"));
        userList.add(new User("userOne", "userOne", "groupOne"));
        userList.add(new User("userTwo", "userTwo", "groupOne"));
        userList.add(new User("userThree", "userThree", "groupTwo"));

        totalSize = TotalSize;
        blockTotalSize = BlockTotalSize;
        keepTotalSize = KeepTotalSize;
        totalBlockNum = TotalBlockNum;
        blockNum = BlockNum;
        keepBlockNum = KeepBlockNum;
        bootBlockPos = BootBlockPos;
        superBlockPos = SuperBlockPos;
        inodePos = InodePos;
        blockPos = BlockPos;
    }
}
