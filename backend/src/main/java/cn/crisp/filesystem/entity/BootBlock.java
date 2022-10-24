package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cn.crisp.filesystem.common.Constants.*;

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
