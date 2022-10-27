package cn.crisp.filesystem.system;


import cn.crisp.filesystem.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.crisp.filesystem.common.Constants.InodeNum;


@Slf4j
@Component
@Data
@AllArgsConstructor
public class FileSystem implements Serializable {
    public static final long serialVersionUID = 7L;
    private  BootBlock bootBlock;
    private SuperBlock superBlock ;
    private ConcurrentHashMap<Integer, Inode> inodes;
    private ConcurrentHashMap<Integer, String> blockInfo;
    private DirTree dirTree;

    public  FileSystem() throws Exception {
        bootBlock = new BootBlock();
        superBlock = new SuperBlock();
        inodes = new ConcurrentHashMap<>();
        dirTree = new DirTree();

        //创建根结点
        superBlock.getInodeMap().getInodeMap()[0] = true;
        superBlock.setInodeNum(superBlock.getInodeNum() + 1);
        Inode root = new Inode();
        root.setId(0);
        root.setIsDir(1);
        root.setName("root");
        root.setCreateBy("system");
        root.setLength(bootBlock.getUserList().size());
        dirTree.setInode(root);
        inodes.put(root.getId(), root);
        for (User user : bootBlock.getUserList()) {
            Inode inode = new Inode();
            //检测哪块空闲
            for (int i = 0; i < InodeNum; i++) {
                if (!superBlock.getInodeMap().getInodeMap()[i]) {
                    superBlock.getInodeMap().getInodeMap()[i] = true;
                    inode.setId(i);
                    break;
                }
            }

            superBlock.setInodeNum(superBlock.getInodeNum() + 1);
            inode.setName(user.getUsername());
            inode.setCreateBy(user.getUsername());
            inode.setIsDir(1);
            inodes.put(inode.getId(), inode);

            //加入根目录
            DirTree tmpDirTree = new DirTree();
            tmpDirTree.setParent(dirTree);
            tmpDirTree.setInode(inode);
            dirTree.getNext().add(tmpDirTree);
            dirTree.getInode().setLength(dirTree.getInode().getLength() + 1);
        }

        log.info("初始化完成");
    }



}
