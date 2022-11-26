package cn.crisp.filesystem.service;

import cn.crisp.filesystem.common.CODE;
import cn.crisp.filesystem.common.R;
import cn.crisp.filesystem.dto.ChangePathDto;
import cn.crisp.filesystem.entity.DirTree;
import cn.crisp.filesystem.entity.Inode;
import cn.crisp.filesystem.entity.User;
import cn.crisp.filesystem.exception.SystemLockException;
import cn.crisp.filesystem.system.FileSystem;
import cn.crisp.filesystem.utils.RedisCache;
import cn.crisp.filesystem.vo.FileVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static cn.crisp.filesystem.common.Constants.*;

@Slf4j
@Service
public class FileSystemService {
    //模拟硬盘
    File file = new File(DISK);

    //缓存
    @Autowired
    RedisCache redisCache;

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

    //jvm锁，系统信息读写并发控制
    private final ReentrantLock disk_lock = new ReentrantLock();

    @SneakyThrows
    public FileSystem check(FileSystem fileSystem) {
        if (!disk_lock.tryLock(2, TimeUnit.SECONDS)) {
            throw new SystemLockException(CODE.SystemLockError, "别的用户正在读写系统，请稍后再试");
        }else {
            try {
                Thread.sleep(3000);
                fileSystem = readFileSystem(fileSystem);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                disk_lock.unlock();
            }
        }
        return fileSystem;
    }

    @SneakyThrows
    public void save(FileSystem fileSystem) {
        if (!disk_lock.tryLock(2, TimeUnit.SECONDS)) {
            throw new SystemLockException(CODE.SystemLockError, "别的用户正在读写系统，请稍后再试");
        }
        else {
            try {
                Thread.sleep(3000);
                writeFileSystem(fileSystem);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                disk_lock.unlock();
            }
        }
    }
    /**
     * 返回文件系统总大小，已创建文件目录总数, 即i结点个数为，已经使用的磁盘块个数，剩余的磁盘块个数，剩余空间大小，启动块在磁盘的偏移，超级块在磁盘的偏移，i结点在磁盘的偏移，可用磁盘区在磁盘的偏移。
     * @param fileSystem
     * @return
     */


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
                        return R.error("路径错误");
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

    /**
     * 先获取目录树根结点，之后dfs至对应结点，之后遍历该目录树结点的子节点数组，返回目录或文件的具体信息，包括文件名，id，内容磁盘块位置，间接索引的详细磁盘块位置，文件权限，文件长度或目录文件个数，创建者，创建时间，以及是否为目录。
     * @param path
     * @param fileSystem
     * @return
     */
    //获取当前目录的所有文件
    public List<FileVo> getDir(String path, FileSystem fileSystem) {
        //获取锁
        while (!redisCache.setnx(path, "getDir", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<FileVo> ret = new ArrayList<>();
        try {
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
                List<Integer> indirect = new ArrayList<>();
                if (d.getInode().getAddress()[10] != -1) {
                    String[] pos = fileSystem.getBlockInfo().get(d.getInode().getAddress()[10]).split(",");
                    for (String s : pos) {
                        indirect.add(Integer.parseInt(s));
                    }
                }
                ret.add(new FileVo(d.getInode().getName(),
                        d.getInode().getId(),
                        d.getInode().getAddress(),
                        indirect,
                        d.getInode().getLimit(),
                        d.getInode().getLength(),
                        d.getInode().getCreateBy(),
                        d.getInode().getCreateTime(),
                        d.getInode().getIsDir()));
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (redisCache.getCacheObject(path) != null &&redisCache.getCacheObject(path).equals("getDir")) redisCache.deleteObject(path);
        }

        return ret;
    }

    /**
     * dfs+bfs，先获取目录树根节点，dfs到对应的目录结点，之后进行bfs，将根目录结点入队，每次访问队头的子节点，将子节点信息写入返回结果，同时将目录结点入队，直至对列为空，返回结果，返回目录或文件的具体信息，包括文件名，id，内容磁盘块位置，间接索引的详细磁盘块位置，文件权限，文件长度或目录文件个数，创建者，创建时间，以及是否为目录。
     * @param path
     * @param fileSystem
     * @return
     */
    //获取目录下所有文件，包括子文件，算法：bfs
    public List<FileVo> getDirs(String path, FileSystem fileSystem) {
        //获取锁
        while (!redisCache.setnx(path, "getDirs", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<FileVo> ret = new ArrayList<>();
        try {
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
            while (deque.size() > 0) {
                DirTree pp = deque.getFirst();
                deque.removeFirst();
                for (DirTree d : pp.getNext()) {
                    if (d.getInode().getIsDir() == 1) deque.addLast(d);
                    List<Integer> indirect = new ArrayList<>();
                    if (d.getInode().getAddress()[10] != -1) {
                        String[] pos = fileSystem.getBlockInfo().get(d.getInode().getAddress()[10]).split(",");
                        for (String s : pos) {
                            indirect.add(Integer.parseInt(s));
                        }
                    }
                    ret.add(new FileVo(d.getInode().getName(),
                            d.getInode().getId(),
                            d.getInode().getAddress(),
                            indirect,
                            d.getInode().getLimit(),
                            d.getInode().getLength(),
                            d.getInode().getCreateBy(),
                            d.getInode().getCreateTime(),
                            d.getInode().getIsDir()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (redisCache.getCacheObject(path) != null &&redisCache.getCacheObject(path).equals("getDirs"))
            redisCache.deleteObject(path);
        }
        return ret;
    }

    /**
     * dfs获取要创建目录的对应目录树结点，之后遍历子节点，判断是否有文件的文件名与要创建的文件名重名，有则返回错误，之后再检测是否有空闲的inode块，有则获取一块并修改系统信息，没有则返回错误，之后修改文件系统inode区相关的信息，同时创建一个新的inode，注入对应的inode信息，创建新的目录树结点加入目录树。
     * @param fileSystem
     * @param path
     * @param username
     * @param filename
     * @return
     */
    //创建目录
    public R<String> makeDir(FileSystem fileSystem, String path, String username, String filename) {
        //获取锁
        while (!redisCache.setnx(path, "makeDir", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
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
                if (d.getInode().getName().equals(filename)) {
                    return R.error("该文件名已被使用");
                }
            }

            Inode inode = new Inode();
            //检测哪块空闲
            for (int i = 0; i < InodeNum; i++) {
                if (!fileSystem.getSuperBlock().getInodeMap().getInodeMap()[i]) {
                    fileSystem.getSuperBlock().getInodeMap().getInodeMap()[i] = true;
                    inode.setId(i);
                    break;
                }
            }
            if (inode.getId() == -1) return R.error("没有空闲的inode块");

            fileSystem.getSuperBlock().setInodeNum(fileSystem.getSuperBlock().getInodeNum() + 1);
            inode.setName(filename);
            inode.setCreateBy(username);
            inode.setIsDir(1);
            fileSystem.getInodes().put(inode.getId(), inode);

            //加入根目录
            DirTree tmpDirTree = new DirTree();
            tmpDirTree.setParent(p);
            tmpDirTree.setInode(inode);
            p.getNext().add(tmpDirTree);
            p.getInode().setLength(p.getInode().getLength() + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("系统错误");
        } finally {
            if (redisCache.getCacheObject(path) != null &&redisCache.getCacheObject(path).equals("makeDir"))
            redisCache.deleteObject(path);
        }

        return R.success("创建成功");
    }


    //判断目录下以及该目录是否有文件不属于该用户，采用bfs
    public R<DirTree> checkFilesBelong(String path, String username, FileSystem fileSystem) {

        //获取锁
        while (!redisCache.setnx(path, "checkBelong", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
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

            if ("root".equals(username) || "system".equals(username)) {
                return R.success(p);
            }

            Deque<DirTree> deque = new ArrayDeque<>();
            deque.addLast(p);

            while (deque.size() > 0) {
                DirTree d = deque.getFirst();
                deque.removeFirst();
                if (!d.getInode().getCreateBy().equals(username)) {
                    return R.error("用户: " + username + "没有权限删除 " + d.getInode().getName());
                }

                for (DirTree son : d.getNext()) {
                    deque.addLast(son);
                }
            }

            return R.success(p);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (redisCache.getCacheObject(path) != null && redisCache.getCacheObject(path).equals("checkBelong"))
            redisCache.deleteObject(path);
        }

        return R.error("内部错误");
    }

    //删除目录及目录下的内容，递归删除
    public Integer removeDir(DirTree dirTree,  FileSystem fileSystem) {
        Integer ret = 1;

        for (DirTree d : dirTree.getNext()) {
            ret += removeDir(d, fileSystem);
        }


        dirTree.setParent(null);

        Inode inode = dirTree.getInode();
        fileSystem.getInodes().remove(inode.getId());
        fileSystem.getSuperBlock().setInodeNum(fileSystem.getSuperBlock().getInodeNum() - 1);
        fileSystem.getSuperBlock().getInodeMap().getInodeMap()[inode.getId()] = false;

        //删除直接索引
        for (int i = 0; i < 10; i++) {
            if (inode.getAddress()[i] == -1) {
                break;
            }
            fileSystem.getBlockInfo().remove(inode.getAddress()[i]);
            fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() - 1);
            fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() + 1);
            fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() + BlockSize);
            fileSystem.getSuperBlock().getBlockMap().getBlockMap()[inode.getAddress()[i]] = false;
            inode.getAddress()[i] = -1;
        }


        //删除间接索引, 间接索引格式"1,2,3,"
        if (inode.getAddress()[10] != -1) {
            String info = fileSystem.getBlockInfo().get(inode.getAddress()[10]);

            fileSystem.getBlockInfo().remove(inode.getAddress()[10]);
            fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() - 1);
            fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() + 1);
            fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() + BlockSize);
            fileSystem.getSuperBlock().getBlockMap().getBlockMap()[inode.getAddress()[10]] = false;
            inode.getAddress()[10] = -1;

            String[] blocks = info.split(",");

            for (String block : blocks) {
                int curBlock = Integer.parseInt(block);
                fileSystem.getBlockInfo().remove(curBlock);
                fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() - 1);
                fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() + 1);
                fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() + BlockSize);
                fileSystem.getSuperBlock().getBlockMap().getBlockMap()[curBlock] = false;
            }
        }


        return ret;
    }

    //从父节点移除该结点
    public FileSystem removeFromParent(FileSystem fileSystem, String path) {
        String[] t = path.split("/");
        DirTree p = fileSystem.getDirTree();
        DirTree lst = p;
        for (String s : t) {
            for (DirTree d : p.getNext()) {
                if (Objects.equals(d.getInode().getName(), s)) {
                    lst = p;
                    p = d;
                    break;
                }
            }
        }
        lst.getNext().remove(p);
        return fileSystem;
    }


    //创建文件
    public R<String> newFile(FileSystem fileSystem, String path, String username, String filename) {
        //获取锁
        while (!redisCache.setnx(path, "newFile", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
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
                if (d.getInode().getName().equals(filename)) {
                    return R.error("该文件名已被使用");
                }
            }

            Inode inode = new Inode();
            //检测哪块空闲
            for (int i = 0; i < InodeNum; i++) {
                if (!fileSystem.getSuperBlock().getInodeMap().getInodeMap()[i]) {
                    fileSystem.getSuperBlock().getInodeMap().getInodeMap()[i] = true;
                    inode.setId(i);
                    break;
                }
            }
            if (inode.getId() == -1) return R.error("没有空闲的inode块");

            fileSystem.getSuperBlock().setInodeNum(fileSystem.getSuperBlock().getInodeNum() + 1);
            inode.setName(filename);
            inode.setCreateBy(username);
            inode.setIsDir(0);
            fileSystem.getInodes().put(inode.getId(), inode);

            //加入根目录
            DirTree tmpDirTree = new DirTree();
            tmpDirTree.setParent(p);
            tmpDirTree.setInode(inode);
            p.getNext().add(tmpDirTree);
            p.getInode().setLength(p.getInode().getLength() + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("内部错误");
        } finally {
            if (redisCache.getCacheObject(path) != null &&redisCache.getCacheObject(path).equals("newFile"))
            redisCache.deleteObject(path);
        }

        return R.success("创建成功");
    }

    Integer readCount = 0;
    //读取文件
    public R<String> catFile(FileSystem fileSystem, String path, String group, String filename) {
        //获取锁
        while (!redisCache.setnx(path, "catFile", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            readCount ++;
            if (readCount == 1) {
                redisCache.setnx(path + "writer", "writeFile", 60L, TimeUnit.SECONDS);
            }

            if (redisCache.getCacheObject(path) != null && redisCache.getCacheObject(path).equals("catFile"))
                redisCache.deleteObject(path);


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

            Inode inode = new Inode();

            for (DirTree d : p.getNext()) {
                if (d.getInode().getName().equals(filename)) {
                    inode = d.getInode();
                    break;
                }
            }

            if (inode.getId() == -1) {
                return R.error("改文件不存在");
            }

            if (inode.getIsDir() == 1) {
                return R.error(inode.getName() + "为目录");
            }


            StringBuilder ret = new StringBuilder();

            //读取直接索引
            for (int i = 0; i < 10; i++) {
                if (inode.getAddress()[i] == -1) break;
                ret.append(fileSystem.getBlockInfo().get(inode.getAddress()[i]));
            }

            //读取间接索引
            if (inode.getAddress()[10] != -1) {
                String posBlock = fileSystem.getBlockInfo().get(inode.getAddress()[10]);
                String[] pos = posBlock.split(",");

                for (String s : pos) {
                    ret.append(fileSystem.getBlockInfo().get(Integer.parseInt(s)));
                }
            }

            return R.success(ret.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            while (!redisCache.setnx(path, "catFile", 60L, TimeUnit.SECONDS)) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            readCount --;
            if (readCount == 0) {
                if (redisCache.getCacheObject(path + "writer") != null && redisCache.getCacheObject(path + "writer").equals("writeFile"))
                    redisCache.deleteObject(path + "writer");
            }
            if (redisCache.getCacheObject(path) != null &&redisCache.getCacheObject(path).equals("catFile"))
            redisCache.deleteObject(path);
        }
        return R.error("系统内部错误");
    }


    /**
     * 先dfs至目标结点，之后遍历子节点，判断文件是否存在或者文件为目录，是则返回错误，之后再判断是否有权限写入文件，没有权限则返回权限错误，读取原来文件结点的内容地址，先删除直接索引，再删除间接索引，最后按块大小截取要写入的内容，按序写入到磁盘块中，如果写入块数超过10块，则采用间接索引的方式，将新索引的结点写入到i结点address的第11个块中，同时在新结点的磁盘块写入内容，如果没有空闲的磁盘块或者超过最大大小，则返回错误。
     * @param fileSystem
     * @param path
     * @param group
     * @param filename
     * @param data
     * @return
     */
    //写入文件
    public R<String> writeFile(FileSystem fileSystem, String path, String group,String filename, String data) {
        //获取锁
        while (!redisCache.setnx(path + "writer", "writeFile", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
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

            Inode inode = new Inode();

            for (DirTree d : p.getNext()) {
                if (d.getInode().getName().equals(filename)) {
                    inode = d.getInode();
                    break;
                }
            }

            if (inode.getId() == -1) {
                return R.error("改文件不存在");
            }

            if (inode.getIsDir() == 1) {
                return R.error(inode.getName() + "为目录");
            }

            String fG = "";

            for (User user : fileSystem.getBootBlock().getUserList()) {
                if (user.getUsername().equals(inode.getCreateBy())) {
                    fG = user.getGroup();
                }
            }

            if (!fG.equals(group)) {
                return R.error("没有权限写入别的组的用户文件");
            }

            //先从系统中移除对应的文件
            //删除直接索引
            for (int i = 0; i < 10; i++) {
                if (inode.getAddress()[i] == -1) {
                    break;
                }
                fileSystem.getBlockInfo().remove(inode.getAddress()[i]);
                fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() - 1);
                fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() + 1);
                fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() + BlockSize);
                fileSystem.getSuperBlock().getBlockMap().getBlockMap()[inode.getAddress()[i]] = false;
                inode.getAddress()[i] = -1;
            }


            //删除间接索引, 间接索引格式"1,2,3,"
            if (inode.getAddress()[10] != -1) {
                String info = fileSystem.getBlockInfo().get(inode.getAddress()[10]);

                fileSystem.getBlockInfo().remove(inode.getAddress()[10]);
                fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() - 1);
                fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() + 1);
                fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() + BlockSize);
                fileSystem.getSuperBlock().getBlockMap().getBlockMap()[inode.getAddress()[10]] = false;
                inode.getAddress()[10] = -1;

                String[] blocks = info.split(",");

                for (String block : blocks) {
                    int curBlock = Integer.parseInt(block);
                    fileSystem.getBlockInfo().remove(curBlock);
                    fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() - 1);
                    fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() + 1);
                    fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() + BlockSize);
                    fileSystem.getSuperBlock().getBlockMap().getBlockMap()[curBlock] = false;
                }
            }


            //写入文件
            int fileIdx = 0;
            int addressIdx = 0;

            while (fileIdx < data.length()) {
                int right = Math.min(data.length(), fileIdx + BlockSize);
                int newBlockIdx = -1;

                for (int i = 0; i < fileSystem.getSuperBlock().getBlockMap().getBlockMap().length; i++) {
                    if (!fileSystem.getSuperBlock().getBlockMap().getBlockMap()[i]) {
                        fileSystem.getSuperBlock().getBlockMap().getBlockMap()[i] = true;
                        newBlockIdx = i;
                        break;
                    }
                }

                if (newBlockIdx == -1) return R.error("磁盘空间不够，部分内容写入失败");

                //直接索引
                if (addressIdx < 10) {
                    inode.getAddress()[addressIdx] = newBlockIdx;

                    fileSystem.getBlockInfo().put(newBlockIdx, data.substring(fileIdx, right));
                    fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
                    fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
                    fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);

                    addressIdx++;

                } else {
                    //间接索引
                    //没有分配则直接分配间接索引结点
                    if (inode.getAddress()[10] == -1) {
                        int newDataBlock = -1;

                        for (int i = 0; i < fileSystem.getSuperBlock().getBlockMap().getBlockMap().length; i++) {
                            if (!fileSystem.getSuperBlock().getBlockMap().getBlockMap()[i]) {
                                fileSystem.getSuperBlock().getBlockMap().getBlockMap()[i] = true;
                                newDataBlock = i;
                                break;
                            }
                        }

                        if (newDataBlock == -1) return R.error("磁盘空间不够，部分内容写入失败");

                        //先分配间接索引结点
                        inode.getAddress()[addressIdx] = newBlockIdx;
                        fileSystem.getBlockInfo().put(newBlockIdx, String.valueOf(newDataBlock) + ",");
                        fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
                        fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
                        fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);

                        //再分配数据结点
                        fileSystem.getBlockInfo().put(newDataBlock, data.substring(fileIdx, right));
                        fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
                        fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
                        fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);
                    } else {
                        //否则先修改原来的间接结点，再写入磁盘块
                        fileSystem.getBlockInfo().put(inode.getAddress()[10], fileSystem.getBlockInfo().get(inode.getAddress()[10]) + String.valueOf(newBlockIdx) + ",");
                        fileSystem.getBlockInfo().put(newBlockIdx, data.substring(fileIdx, right));
                        fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
                        fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
                        fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);
                    }
                }

                fileIdx = right;
            }
            inode.setLength(data.length());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("系统内部错误");
        } finally {
            if (redisCache.getCacheObject(path + "writer") != null && redisCache.getCacheObject(path + "writer").equals("writeFile"))
                redisCache.deleteObject(path + "writer");
        }

        return R.success("写入文件成功");
    }



    public R<String> delFile(FileSystem fileSystem, String path, String username, String filename) {
        //获取锁
        while (!redisCache.setnx(path, "delFile", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
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

            Inode inode = null;
            DirTree dirTree = null;

            for (DirTree d : p.getNext()) {
                if (d.getInode().getName().equals(filename)) {
                    dirTree = d;
                    inode = d.getInode();
                    break;
                }
            }

            if (inode == null) {
                return R.error("没有该文件");
            }

            if (inode.getIsDir() == 1) {
                return R.error("该文件为目录文件");
            }

            if (!inode.getCreateBy().equals(username)) {
                return R.error("无权限删除别的用户内容");
            }

            dirTree.setParent(null);

            fileSystem.getInodes().remove(inode.getId());
            fileSystem.getSuperBlock().setInodeNum(fileSystem.getSuperBlock().getInodeNum() - 1);
            fileSystem.getSuperBlock().getInodeMap().getInodeMap()[inode.getId()] = false;

            //删除直接索引
            for (int i = 0; i < 10; i++) {
                if (inode.getAddress()[i] == -1) {
                    break;
                }
                fileSystem.getBlockInfo().remove(inode.getAddress()[i]);
                fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() - 1);
                fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() + 1);
                fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() + BlockSize);
                fileSystem.getSuperBlock().getBlockMap().getBlockMap()[inode.getAddress()[i]] = false;
                inode.getAddress()[i] = -1;
            }


            //删除间接索引, 间接索引格式"1,2,3,"
            if (inode.getAddress()[10] != -1) {
                String info = fileSystem.getBlockInfo().get(inode.getAddress()[10]);

                fileSystem.getBlockInfo().remove(inode.getAddress()[10]);
                fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() - 1);
                fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() + 1);
                fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() + BlockSize);
                fileSystem.getSuperBlock().getBlockMap().getBlockMap()[inode.getAddress()[10]] = false;
                inode.getAddress()[10] = -1;

                String[] blocks = info.split(",");

                for (String block : blocks) {
                    int curBlock = Integer.parseInt(block);
                    fileSystem.getBlockInfo().remove(curBlock);
                    fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() - 1);
                    fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() + 1);
                    fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() + BlockSize);
                    fileSystem.getSuperBlock().getBlockMap().getBlockMap()[curBlock] = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (redisCache.getCacheObject(path) != null &&  redisCache.getCacheObject(path).equals("delFile"))
                redisCache.deleteObject(path);
        }

        return R.success("删除成功");
    }


    //检测复制目录下有无非同组的文件
    public R<String> checkFileGroup(FileSystem fileSystem, String path, String fileName, String group) {
        String oldPath = path;
        //获取锁
        while (!redisCache.setnx(oldPath, "checkFileGroup", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            if (!path.equals("/")) {
                path += "/";
            }
            path += fileName;

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

            if ("root".equals(group)) {
                return R.success("root组拥有所有权限");
            }

            Deque<DirTree> deque = new ArrayDeque<>();
            deque.addLast(p);

            while (deque.size() > 0) {
                DirTree d = deque.getFirst();
                deque.removeFirst();

                String fG = "";

                for (User user : fileSystem.getBootBlock().getUserList()) {
                    if (user.getUsername().equals(d.getInode().getCreateBy())) {
                        fG = user.getGroup();
                        break;
                    }
                }

                if (!fG.equals(group)) {
                    return R.error("没有权限进入 " + d.getInode().getName());
                }

                for (DirTree son : d.getNext()) {
                    deque.addLast(son);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (redisCache.getCacheObject(oldPath) != null && redisCache.getCacheObject(oldPath).equals("checkFileGroup"))
                redisCache.deleteObject(oldPath);
        }

        return R.success("拥有该文件r权限");
    }


    /**
     * 算法：递归+dfs，终止条件为传入的目录参数为空，否则则创建新的inode结点，将原结点的内容复制一遍，如果为文件，则还要读取文件直接索引以及间接索引，读取所有的内容写入到新的磁盘块中，中途出现磁盘块不够则返回错误，将信息更新到文件系统中。
     * @param fileSystem
     * @param fromPath
     * @param toPath
     * @param fileFromName
     * @param fileToName
     * @param username
     * @return
     */
    //文件系统内部复制
    public R<String> copyFileInside(FileSystem fileSystem, String fromPath, String toPath, String fileFromName, String fileToName, String username) {
        //获取锁
        while (!redisCache.setnx(fromPath + "from", "copyFile", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //获取锁
        while (!redisCache.setnx(toPath + "to", "copyFile", 60L, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {


            if (fileToName.length() == 0) fileToName = fileFromName;

            //获取源文件目录树位置
            if (!fromPath.equals("/")) {
                fromPath += "/";
            }
            fromPath += fileFromName;

            String[] ft = fromPath.split("/");
            DirTree from = fileSystem.getDirTree();
            for (String s : ft) {
                for (DirTree d : from.getNext()) {
                    if (Objects.equals(d.getInode().getName(), s)) {
                        from = d;
                        break;
                    }
                }
            }

            //获取目标文件目录树位置
            String[] t = toPath.split("/");
            DirTree to = fileSystem.getDirTree();
            for (String s : t) {
                for (DirTree d : to.getNext()) {
                    if (Objects.equals(d.getInode().getName(), s)) {
                        to = d;
                        break;
                    }
                }
            }
            //检查是否存在
            for (DirTree d : to.getNext()) {
                if (d.getInode().getName().equals(fileToName)) {
                    return R.error("文件: " + fileToName + " 已存在");
                }
            }

            DirTree newNode = copyDFS(fileSystem, from, fileToName, username);

            newNode.setParent(to);

            to.getNext().add(newNode);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("内部错误");
        } finally {
            if (redisCache.getCacheObject(fromPath + "from") != null &&redisCache.getCacheObject(fromPath + "from").equals("copyFile"))
                redisCache.deleteObject(fromPath +"from");
            if (redisCache.getCacheObject(toPath + "to") != null &&redisCache.getCacheObject(toPath + "to").equals("copyFile"))
                redisCache.deleteObject(toPath + "to");
        }

        return R.success("复制成功");
    }

    //内部复制，采用dfs递归
    public DirTree copyDFS(FileSystem fileSystem, DirTree from, String filename, String username) {
        if (from == null) return null;

        Inode fromNode = from.getInode();

        DirTree ret = new DirTree();

        Inode inode = new Inode();
        //检测哪块空闲
        for (int i = 0; i < InodeNum; i++) {
            if (!fileSystem.getSuperBlock().getInodeMap().getInodeMap()[i]) {
                fileSystem.getSuperBlock().getInodeMap().getInodeMap()[i] = true;
                inode.setId(i);
                break;
            }
        }
        if (inode.getId() == -1) return null;

        //复制基本信息
        fileSystem.getSuperBlock().setInodeNum(fileSystem.getSuperBlock().getInodeNum() + 1);
        inode.setName(filename);
        inode.setCreateBy(username);
        inode.setIsDir(fromNode.getIsDir());
        inode.setLength(fromNode.getLength());

        //复制磁盘块信息
        //直接索引
        for (int i = 0; i < 10; i ++) {
            if (fromNode.getAddress()[i] == -1) {
                break;
            }
            int newBlockIdx = -1;
            for (int j = 0; j < fileSystem.getSuperBlock().getBlockMap().getBlockMap().length; j ++) {
                if (!fileSystem.getSuperBlock().getBlockMap().getBlockMap()[j]) {
                    fileSystem.getSuperBlock().getBlockMap().getBlockMap()[j] = true;
                    newBlockIdx = j;
                    break;
                }
            }

            if (newBlockIdx == -1) return null;

            inode.getAddress()[i] = newBlockIdx;

            fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
            fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
            fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);

            fileSystem.getBlockInfo().put(newBlockIdx, fileSystem.getBlockInfo().get(fromNode.getAddress()[i]));
        }

        //间接索引
        if (fromNode.getAddress()[10] != -1) {
            String info = fileSystem.getBlockInfo().get(fromNode.getAddress()[10]);
            String[] pos = info.split(",");

            //先分配间接索引结点
            int newBlockIdx = -1;
            for (int j = 0; j < fileSystem.getSuperBlock().getBlockMap().getBlockMap().length; j ++) {
                if (!fileSystem.getSuperBlock().getBlockMap().getBlockMap()[j]) {
                    fileSystem.getSuperBlock().getBlockMap().getBlockMap()[j] = true;
                    newBlockIdx = j;
                    break;
                }
            }

            inode.getAddress()[10] = newBlockIdx;
            fileSystem.getBlockInfo().put(inode.getAddress()[10], "");
            fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
            fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
            fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);

            //再分配间接索引的具体磁盘块信息
            for (String p : pos) {
                int newDataIdx = -1;
                for (int j = 0; j < fileSystem.getSuperBlock().getBlockMap().getBlockMap().length; j ++) {
                    if (!fileSystem.getSuperBlock().getBlockMap().getBlockMap()[j]) {
                        fileSystem.getSuperBlock().getBlockMap().getBlockMap()[j] = true;
                        newDataIdx = j;
                        break;
                    }
                }

                fileSystem.getBlockInfo().put(inode.getAddress()[10], fileSystem.getBlockInfo().get(inode.getAddress()[10]) + String.valueOf(newDataIdx) + ",");
                fileSystem.getBlockInfo().put(newDataIdx, fileSystem.getBlockInfo().get(Integer.parseInt(p)));
                fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
                fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
                fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);
            }
        }

        fileSystem.getInodes().put(inode.getId(), inode);

        ret.setInode(inode);

        //递归设置i结点
        for (DirTree son : from.getNext()) {
            DirTree newSon = copyDFS(fileSystem, son, son.getInode().getName(), username);
            newSon.setParent(ret);
            ret.getNext().add(newSon);
        }

        return ret;
    }


    /**
     * 先通过路径的第一个字符判断哪个系统，如果字符为/则为unix文件系统，则读取直接索引以及间接索引的内容放入data，否则读取windows系统的文件内容，写入data，同理判断是否为unix文件系统，如果是则同newfile创建文件，再写入到磁盘块，创建直接索引、间接索引等内容，否则写入windows系统。
     * @param fileSystem
     * @param fromPath
     * @param toPath
     * @param fileFromName
     * @param fileToName
     * @param username
     * @return
     */
    //文件系统之间复制
    public R<String> simdiskCopy(FileSystem fileSystem, String fromPath,  String toPath, String fileFromName, String fileToName, String username) {
        if (fileToName.length() == 0) fileToName = fileFromName;
        String data = "";
        if (fromPath.charAt(0) != '/'){
            File file = new File(fromPath);
            if (!file.exists()) {
                return R.error("文件不存在");
            }
            try {
                data = Files.readString(Paths.get(fromPath));
            }
            catch (Exception e) {
                e.printStackTrace();
                return R.error("请检查文件路径");
            }
        }
        else {
            //获取源文件目录树位置
            if (!fromPath.equals("/")) {
                fromPath += "/";
            }
            fromPath += fileFromName;

            String[] ft = fromPath.split("/");
            DirTree from = fileSystem.getDirTree();
            for (String s : ft) {
                for (DirTree d : from.getNext()) {
                    if (Objects.equals(d.getInode().getName(), s)) {
                        from = d;
                        break;
                    }
                }
            }

            //检查是否为文件
            if (from.getInode().getIsDir() == 1) {
                return R.error("系统间不支持目录拷贝");
            }

            //读取文件内容
            //直接索引
            for (int i = 0; i < 10; i ++) {
                if (from.getInode().getAddress()[i] == -1) {
                    break;
                }
                data += fileSystem.getBlockInfo().get(from.getInode().getAddress()[i]);
            }

            //间接索引
            if (from.getInode().getAddress()[10] != -1) {
                String info = fileSystem.getBlockInfo().get(from.getInode().getAddress()[10]);
                String[] blocks = info.split(",");
                for (String s : blocks) {
                    data += fileSystem.getBlockInfo().get(Integer.parseInt(s));
                }
            }
        }

        if (toPath.charAt(0) != '/') {
            File file = new File(toPath);
            try {
                if (!file.exists()) file.createNewFile();

                FileOutputStream f = new FileOutputStream(file);
                ObjectOutputStream o = new ObjectOutputStream(f);
                o.writeChars(data);
                o.close();
                f.close();

            } catch (Exception e) {
                e.printStackTrace();
                return R.error("创建文件失败");
            }

        }
        else {
            //获取目标文件目录树位置
            String[] t = toPath.split("/");
            DirTree to = fileSystem.getDirTree();
            for (String s : t) {
                for (DirTree d : to.getNext()) {
                    if (Objects.equals(d.getInode().getName(), s)) {
                        to = d;
                        break;
                    }
                }
            }
            //检查是否存在
            for (DirTree d : to.getNext()) {
                if (d.getInode().getName().equals(fileToName)) {
                    return R.error("文件: " + fileToName + " 已存在");
                }
            }

            DirTree newNode = new DirTree();

            Inode inode = new Inode();
            //检测哪块空闲
            for (int i = 0; i < InodeNum; i++) {
                if (!fileSystem.getSuperBlock().getInodeMap().getInodeMap()[i]) {
                    fileSystem.getSuperBlock().getInodeMap().getInodeMap()[i] = true;
                    inode.setId(i);
                    break;
                }
            }
            if (inode.getId() == -1) return R.error("inode区已满");
            inode.setIsDir(0);
            inode.setName(fileToName);
            inode.setCreateBy(username);
            inode.setLength(data.length());



            fileSystem.getInodes().put(inode.getId(), inode);
            fileSystem.getSuperBlock().setInodeNum(fileSystem.getSuperBlock().getInodeNum() + 1);

            //写入文件
            int fileIdx = 0;
            int addressIdx = 0;

            while (fileIdx < data.length()) {
                int right = Math.min(data.length(), fileIdx + BlockSize);
                int newBlockIdx = -1;

                for (int i = 0; i < fileSystem.getSuperBlock().getBlockMap().getBlockMap().length; i ++) {
                    if (!fileSystem.getSuperBlock().getBlockMap().getBlockMap()[i]) {
                        fileSystem.getSuperBlock().getBlockMap().getBlockMap()[i] = true;
                        newBlockIdx = i;
                        break;
                    }
                }

                if (newBlockIdx == -1) return R.error("磁盘空间不够，部分内容写入失败");

                //直接索引
                if (addressIdx < 10) {
                    inode.getAddress()[addressIdx] = newBlockIdx;

                    fileSystem.getBlockInfo().put(newBlockIdx, data.substring(fileIdx, right));
                    fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
                    fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
                    fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);

                    addressIdx++;

                }
                else {
                    //间接索引
                    //没有分配则直接分配间接索引结点
                    if (inode.getAddress()[10] == -1) {
                        int newDataBlock = -1;

                        for (int i = 0; i < fileSystem.getSuperBlock().getBlockMap().getBlockMap().length; i ++) {
                            if (!fileSystem.getSuperBlock().getBlockMap().getBlockMap()[i]) {
                                fileSystem.getSuperBlock().getBlockMap().getBlockMap()[i] = true;
                                newDataBlock = i;
                                break;
                            }
                        }

                        if (newDataBlock == -1) return R.error("磁盘空间不够，部分内容写入失败");

                        //先分配间接索引结点
                        inode.getAddress()[addressIdx] = newBlockIdx;
                        fileSystem.getBlockInfo().put(newBlockIdx, String.valueOf(newDataBlock) + ",");
                        fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
                        fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
                        fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);

                        //再分配数据结点
                        fileSystem.getBlockInfo().put(newDataBlock, data.substring(fileIdx, right));
                        fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
                        fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
                        fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);
                    }
                    else {
                        //否则先修改原来的间接结点，再写入磁盘块
                        fileSystem.getBlockInfo().put(inode.getAddress()[10], fileSystem.getBlockInfo().get(inode.getAddress()[10]) + String.valueOf(newBlockIdx) + ",");
                        fileSystem.getBlockInfo().put(newBlockIdx, data.substring(fileIdx, right));
                        fileSystem.getSuperBlock().setBlockNum(fileSystem.getSuperBlock().getBlockNum() + 1);
                        fileSystem.getSuperBlock().setBlockFree(fileSystem.getSuperBlock().getBlockFree() - 1);
                        fileSystem.getSuperBlock().setLastBlockSize(fileSystem.getSuperBlock().getLastBlockSize() - BlockSize);
                    }
                }

                fileIdx = right;
            }

            newNode.setInode(inode);

            newNode.setParent(to);

            to.getNext().add(newNode);
        }

        return R.success("复制成功");
    }
}
