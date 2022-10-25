package cn.crisp.filesystem.service;

import cn.crisp.filesystem.system.FileSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.crisp.filesystem.common.Constants.DISK;
import static cn.crisp.filesystem.common.Constants.TotalSize;

@Slf4j
@Service
public class FileSystemService {
    //模拟硬盘
    File file = new File(DISK);

    private FileSystem readFileSystem(FileSystem fileSystem) throws Exception {
        if (!file.exists()) {
            if (file.createNewFile()) {
                log.info("创建DISK成功");
            }
        }
        FileInputStream f = new FileInputStream(file);
        ObjectInputStream o = new ObjectInputStream(f);

        FileSystem fileSystem1 = (FileSystem) o.readObject();
        if (!Objects.equals(fileSystem1.getBootBlock(),fileSystem.getBootBlock())) {
            log.info("权限不一致，已更新");
            fileSystem1.setBootBlock(fileSystem.getBootBlock());
        }
        log.info("读入文件系统成功");
        o.close();
        f.close();
        return fileSystem1;
    }


    private void writeFileSystem(FileSystem fileSystem) throws Exception{
        FileOutputStream f = new FileOutputStream(file);
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(fileSystem);
        log.info("文件系统信息写入硬盘成功");
        o.close();
        f.close();
    }

    public FileSystem test(FileSystem fileSystem) {
        try {
            //writeFileSystem(fileSystem);
            fileSystem = readFileSystem(fileSystem);
        }catch (Exception e){
            e.printStackTrace();
        }
        return fileSystem;
    }

    public List<String> getSystemInfo(FileSystem fileSystem) {
        List<String> ret = new ArrayList<>();

        String str = "系统总大小为: " + TotalSize + "bytes";
        ret.add(str);

        str = "已创建文件目录总数, 即i结点个数为: " + String.valueOf(fileSystem.getSuperBlock().getInodeNum());
        ret.add(str);

        str = "已经使用的磁盘块个数: " + String.valueOf(fileSystem.getSuperBlock().getBlockNum());
        ret.add(str);

        str = "剩余的磁盘块个数: " + String.valueOf(fileSystem.getSuperBlock().getBlockFree());
        ret.add(str);

        str = "剩余空间大小为: " + String.valueOf(fileSystem.getSuperBlock().getLastBlockSize()) + "bytes";
        ret.add(str);

        str = "启动块在磁盘的偏移为: " + String.valueOf(fileSystem.getBootBlock().getBootBlockPos()) + "bytes";
        ret.add(str);

        str = "超级块在磁盘的偏移为: " + String.valueOf(fileSystem.getBootBlock().getSuperBlockPos()) + "bytes";
        ret.add(str);

        str = "i结点在磁盘的偏移为: " + String.valueOf(fileSystem.getBootBlock().getInodePos()) + "bytes";
        ret.add(str);

        str = "可用磁盘区在磁盘的偏移为: " + String.valueOf(fileSystem.getBootBlock().getBlockPos()) + "bytes";
        ret.add(str);
        return ret;
    }


}
