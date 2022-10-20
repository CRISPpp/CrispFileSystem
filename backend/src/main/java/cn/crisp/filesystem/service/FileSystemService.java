package cn.crisp.filesystem.service;

import cn.crisp.filesystem.entity.Fcb;
import cn.crisp.filesystem.system.FileSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Objects;

import static cn.crisp.filesystem.common.Constants.DISK;

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
}
