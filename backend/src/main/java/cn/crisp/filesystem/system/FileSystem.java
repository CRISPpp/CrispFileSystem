package cn.crisp.filesystem.system;


import cn.crisp.filesystem.entity.BootBlock;
import cn.crisp.filesystem.entity.Fcb;
import cn.crisp.filesystem.entity.Inode;
import cn.crisp.filesystem.entity.SuperBlock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.crisp.filesystem.common.Constants.DISK;

@Slf4j
@Component
@Data
@AllArgsConstructor
public class FileSystem implements Serializable {
    private long serialVersionUID = 7L;
    private  BootBlock bootBlock;
    private SuperBlock superBlock ;
    private ConcurrentHashMap<Integer, Inode> inodes;
    private ConcurrentHashMap<Integer, Fcb> fcbs;

    public  FileSystem() throws Exception {
        bootBlock = new BootBlock();
        superBlock = new SuperBlock();
        inodes = new ConcurrentHashMap<>();
        fcbs = new ConcurrentHashMap<>();
        log.info("初始化完成");
    }



}
