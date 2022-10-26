package cn.crisp.filesystem.service;

import cn.crisp.filesystem.common.R;
import cn.crisp.filesystem.dto.ChangePathDto;
import cn.crisp.filesystem.entity.DirTree;
import cn.crisp.filesystem.entity.User;
import cn.crisp.filesystem.system.FileSystem;
import cn.crisp.filesystem.vo.FileVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

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

    public FileSystem check(FileSystem fileSystem) {
        try {
            //writeFileSystem(fileSystem);
            fileSystem = readFileSystem(fileSystem);
        }catch (Exception e){
            e.printStackTrace();
        }
        return fileSystem;
    }

    public void save(FileSystem fileSystem) {
        try {
            writeFileSystem(fileSystem);
        }catch (Exception e){
            e.printStackTrace();
        }
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

    //判断路径是否正确
    public R<String> checkDir(ChangePathDto changePathDto, FileSystem fileSystem) {
        if(StringUtils.isEmpty(changePathDto.getPath())) return R.error("路径不能为空");
        if(Objects.equals(changePathDto.getPath(), "/")) {
            if (Objects.equals(changePathDto.getGroup(), "root")) {
                return R.success("/");
            }
            return R.error("没有权限");
        }

        String[] paths = changePathDto.getPath().split("/");
        if (paths.length == 0) return R.error("路径不能为空");
        //全局路径，相对路径由前端拼凑
        if (paths[0].length() == 0) {
            Deque<String> deque = new ArrayDeque<>();;
            DirTree root = fileSystem.getDirTree();
            DirTree p = root;
            for (int i = 1; i < paths.length; i ++) {
                //判断文件夹是否存在
                if(Objects.equals(paths[i], ".")) {
                    continue;
                }
                if(Objects.equals(paths[i], "..")) {
                    if (p.getParent() == null) {
                        return R.error("路径错误，你在写尼玛");
                    }
                    deque.removeLast();
                    p = p.getParent();
                    continue;
                }
                boolean f = false;
                for (DirTree dirTree : p.getNext()) {
                    if (Objects.equals(dirTree.getInode().getName(), paths[i])) {
                        f = true;
                        p = dirTree;
                        break;
                    }
                }

                if (!f) {
                    return R.error("目录" + paths[i] + " 不存在");
                }

                //1为用户目录，需要特判
                if (p.getParent() == root) {
                    String fG = "";
                    for(User user : fileSystem.getBootBlock().getUserList()) {
                        if (Objects.equals(user.getUsername(), p.getInode().getCreateBy())) {
                            fG = user.getGroup();
                            break;
                        }
                    }
                    if (!(Objects.equals(fG, changePathDto.getGroup()) || Objects.equals(changePathDto.getGroup(), "root"))) {
                        return R.error("没有权限进入别用户组的用户目录");
                    }
                }

                //不是目录，特判
                if (p.getInode().getIsDir() == 0) {
                    return R.error(paths[i] + "非目录");
                }
                deque.addLast("/" + paths[i]);
            }
            StringBuilder ret = new StringBuilder();
            while (deque.size() > 0) {
                ret.append(deque.getFirst());
                deque.removeFirst();
            }
            if(ret.length() == 0) {
                if(!Objects.equals(changePathDto.getGroup(), "root")) {
                    return R.error("没有权限");
                }
                ret.append("/");
            }
            return R.success(ret.toString());
        }

        return R.error("路径错误");
    }

    //获取当前目录的所有文件
    public List<FileVo> getDir(String path, FileSystem fileSystem) {
        List<FileVo> ret = new ArrayList<>();
        String[] t = path.split("/");
        DirTree p = fileSystem.getDirTree();
        for (String s : t) {
            for (DirTree d : p.getNext()) {
                if (Objects.equals(d.getInode().getName(), s)) {
                    p = d;
                    break;
                }
            }
        }

        for (DirTree d : p.getNext()) {
            ret.add(new FileVo(d.getInode().getName(),
                    d.getInode().getId(),
                    d.getInode().getAddress(),
                    d.getInode().getLimit(),
                    d.getInode().getLength(),
                    d.getInode().getCreateBy(),
                    d.getInode().getCreateTime(),
                    d.getInode().getIsDir()));
        }

        return ret;
    }

    //获取目录下所有文件，包括子文件，算法：bfs
    public List<FileVo> getDirs(String path, FileSystem fileSystem) {
        List<FileVo> ret = new ArrayList<>();
        String[] t = path.split("/");
        DirTree p = fileSystem.getDirTree();
        for (String s : t) {
            for (DirTree d : p.getNext()) {
                if (Objects.equals(d.getInode().getName(), s)) {
                    p = d;
                    break;
                }
            }
        }

        Deque<DirTree> deque = new ArrayDeque<>();
        deque.addLast(p);
        while(deque.size() > 0) {
            DirTree pp = deque.getFirst();
            deque.removeFirst();
            for (DirTree d : pp.getNext()) {
                if (d.getInode().getIsDir() == 1) deque.addLast(d);
                ret.add(new FileVo(d.getInode().getName(),
                        d.getInode().getId(),
                        d.getInode().getAddress(),
                        d.getInode().getLimit(),
                        d.getInode().getLength(),
                        d.getInode().getCreateBy(),
                        d.getInode().getCreateTime(),
                        d.getInode().getIsDir()));
            }
        }
        return ret;
    }
}
