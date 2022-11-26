package cn.crisp.filesystem.controller;

import cn.crisp.filesystem.common.R;
import cn.crisp.filesystem.dto.*;
import cn.crisp.filesystem.entity.DirTree;
import cn.crisp.filesystem.entity.User;
import cn.crisp.filesystem.exception.SystemLockException;
import cn.crisp.filesystem.service.FileSystemService;
import cn.crisp.filesystem.system.FileSystem;
import cn.crisp.filesystem.vo.FileVo;
import cn.crisp.filesystem.vo.HelpVo;
import cn.crisp.filesystem.vo.UserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static cn.crisp.filesystem.common.Constants.*;

@Slf4j
@Api(tags = "CrispFileSystem")
@RequestMapping("/sys")
@RestController
public class SystemController {
    @Autowired
    FileSystem fileSystem;

    @Autowired
    FileSystemService fileSystemService;

    @ApiOperation("恢复文件系统")
    @PostMapping("/check")
    public R<String> check(@RequestBody GroupDto groupDto) {
        if (!"root".equals(groupDto.getGroup())) {
            return R.error("没有恢复权限");
        }
        try {
            fileSystem = fileSystemService.check(fileSystem);
        } catch (SystemLockException e) {
            e.printStackTrace();
            return R.error("有别的用户正在读写系统，请稍后再试");
        }

        return R.success("读取成功");
    }

    @ApiOperation("保存文件系统")
    @PostMapping("/save")
    public R<String> save(@RequestBody GroupDto groupDto) {
        if (!"root".equals(groupDto.getGroup())) {
            return R.error("没有保存权限");
        }

        try {
            fileSystemService.save(fileSystem);
        }catch (Exception e) {
            e.printStackTrace();
            return R.error("别的用户正在读写系统，请稍后再试");
        }
        return R.success("保存成功");
    }

    /**
     * 采用遍历的方式，通过启动块获取用户列表，遍历到用户名位置，如果用户名不存在或者存在但是用户密码对不上则返回错误。
     * @param loginDto
     * @return
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R<UserVo> login(@RequestBody LoginDto loginDto){
        User user = null;
        for (User user1 : fileSystem.getBootBlock().getUserList()) {
            if (Objects.equals(user1.getUsername(), loginDto.getUsername())) {
                user = user1;
                break;
            }
        }

        if (user == null) return R.error("用户不存在");

        if (!Objects.equals(user.getPassword(), loginDto.getPassword())) {
            return R.error("密码错误");
        }

        return R.success(new UserVo(user.getUsername(), user.getGroup()));
    }

    @ApiOperation("获取命令列表")
    @GetMapping("/help")
    public R<HelpVo> getCMD() {
        return R.success(new HelpVo(CMDList, CMDDescription));
    }

    /**
     * 显示整个系统信息(参考Linux文件系统的系统信息)，文件可以根据用户进行读写保护。目录名和文件名支持全路径名和相对路径名，路径名各分量间用“/”隔开。
     * @return
     */
    @ApiOperation("获取系统信息")
    @GetMapping("/info")
    public R<List<String>> getInfo() {
        return R.success(fileSystemService.getSystemInfo(fileSystem));
    }


    @ApiOperation("改变目录")
    @PostMapping("/cd")
    public R<String> cd(@RequestBody ChangePathDto changePathDto) {
        return fileSystemService.checkDir(changePathDto, fileSystem);
    }

    /**
     * 获取/sys/dir的post请求，请求体包括用户组、查看的路径、用户名，先调用服务层的checkDir检测用户是否能够进入目标目录以及目标目录是否存在，之后调用服务层的getDir接口获取服务的信息。
     * @param changePathDto
     * @return
     */
    @ApiOperation("查询目录内容")
    @PostMapping("/dir")
    public R<List<FileVo>> dir(@RequestBody ChangePathDto changePathDto) {
        R<String> tmp = fileSystemService.checkDir(changePathDto, fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String path = tmp.getData();
        List<FileVo> ret = fileSystemService.getDir(path, fileSystem);
        return R.success(ret);
    }

    /**
     * 获取/sys/dirs的post请求，请求体包括用户组、查看的路径、用户名，先调用服务层的checkDir检测用户是否能进入对应的目录，之后调用服务层的getDirs获取所有子目录的信息。
     * @param changePathDto
     * @return
     */
    @ApiOperation("查询目录下所有子文件")
    @PostMapping("/dirs")
    public R<List<FileVo>> dirs(@RequestBody ChangePathDto changePathDto) {
        R<String> tmp = fileSystemService.checkDir(changePathDto, fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String path = tmp.getData();
        List<FileVo> ret = fileSystemService.getDirs(path, fileSystem);

        return R.success(ret);
    }

    /**
     * 获取/sys/md的post请求，请求体包括创建的路径以及文件名，用户名以及用户组，先通过‘/’来分离出创建路径以及文件名，之后调用服务层的makeDir创建文件夹并返回结果。
     * @param makeDirDto
     * @return
     */
    @ApiOperation("创建目录")
    @PostMapping("/md")
    public R<String> md(@RequestBody MakeDirDto makeDirDto) {
        StringBuilder tmpPath = new StringBuilder();

        int idx = makeDirDto.getPath().length() - 1;

        while(idx >= 0 && makeDirDto.getPath().charAt(idx) != '/') {
            idx--;
        }

        StringBuilder fileName = new StringBuilder();

        for (int i = idx + 1; i < makeDirDto.getPath().length(); i ++) {
            fileName.append(makeDirDto.getPath().charAt(i));
        }

        for (int i = 0; i < idx; i ++) {
            tmpPath.append(makeDirDto.getPath().charAt(i));
        }
        if(tmpPath.isEmpty()) tmpPath.append("/");
        R<String> tmp = fileSystemService.checkDir(new ChangePathDto(tmpPath.toString(), makeDirDto.getUsername(), makeDirDto.getGroup()), fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String path = tmp.getData();

        return fileSystemService.makeDir(fileSystem, path, makeDirDto.getUsername(), fileName.toString());
    }

    /**
     * 获取/sys/rd的post请求，请求体包括路径，用户名以及用户组，先调用服务层的checkDir检查是否有权限进入目录下，之后再调用服务层的checkFilesBelong检测是否有权限删除文件夹下所有文件，没有问题再调用服务层的移除目录。
     * @param removeDirDto
     * @return
     */
    @ApiOperation("删除目录")
    @PostMapping("/rd")
    public R<Integer> rd(@RequestBody RemoveDirDto removeDirDto) {
        R<String> tmp = fileSystemService.checkDir(new ChangePathDto(removeDirDto.getPath(), removeDirDto.getUsername(), removeDirDto.getGroup()), fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String path = tmp.getData();

        R<DirTree> tmp1 = fileSystemService.checkFilesBelong(path, removeDirDto.getUsername(), fileSystem);
        if (tmp1.getCode() == 0) {
            return R.error(tmp1.getMsg());
        }

        Integer ret = fileSystemService.removeDir(tmp1.getData(), fileSystem);

        fileSystem = fileSystemService.removeFromParent(fileSystem, path);

        return R.success(ret);
    }

    /**
     * 获取/sys/newfile的post请求，请求体为创建路径，用户名以及用户组。首先通过/将创建路径和创建的文件名分割出来，之后调用服务层的checkDir检查用户是否有权限进入要创建的目录，最后调用服务层的newfile接口，返回结果。
     * @param newFileDto
     * @return
     */
    @ApiOperation("创建文件")
    @PostMapping("/newfile")
    public R<String> newfile(@RequestBody NewFileDto newFileDto) {
        StringBuilder tmpPath = new StringBuilder();

        int idx = newFileDto.getPath().length() - 1;

        while(idx >= 0 && newFileDto.getPath().charAt(idx) != '/') {
            idx--;
        }

        StringBuilder fileName = new StringBuilder();

        for (int i = idx + 1; i < newFileDto.getPath().length(); i ++) {
            fileName.append(newFileDto.getPath().charAt(i));
        }

        for (int i = 0; i < idx; i ++) {
            tmpPath.append(newFileDto.getPath().charAt(i));
        }
        if(tmpPath.isEmpty()) tmpPath.append("/");

        R<String> tmp = fileSystemService.checkDir(new ChangePathDto(tmpPath.toString(), newFileDto.getUsername(), newFileDto.getGroup()), fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String path = tmp.getData();

        return fileSystemService.newFile(fileSystem, path, newFileDto.getUsername(), fileName.toString());
    }


    @ApiOperation("打开文件")
    @PostMapping("/cat")
    public R<String> cat(@RequestBody CatDto catDto) {
        StringBuilder tmpPath = new StringBuilder();

        int idx = catDto.getPath().length() - 1;

        while(idx >= 0 && catDto.getPath().charAt(idx) != '/') {
            idx--;
        }

        StringBuilder fileName = new StringBuilder();

        for (int i = idx + 1; i < catDto.getPath().length(); i ++) {
            fileName.append(catDto.getPath().charAt(i));
        }

        for (int i = 0; i < idx; i ++) {
            tmpPath.append(catDto.getPath().charAt(i));
        }
        if(tmpPath.isEmpty()) tmpPath.append("/");

        R<String> tmp = fileSystemService.checkDir(new ChangePathDto(tmpPath.toString(), catDto.getUsername(), catDto.getGroup()), fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String path = tmp.getData();


        return fileSystemService.catFile(fileSystem, path, catDto.getGroup(), fileName.toString());
    }

    /**
     * 获取/sys/writeFile的post请求，请求体包括写入路径，用户名，用户组以及写入的内容。先分割目录路径以及写入文件名，之后调用服务层的checkDir检测是否有权限进入目标目录，之后调用服务层的writeFile写入文件。
     * @param writeDto
     * @return
     */
    @ApiOperation("写入文件")
    @PostMapping("/writeFile")
    public R<String> writeFile(@RequestBody WriteDto writeDto) {
        if (writeDto.getData().length() > FileLimitSize) return R.error("文件大小超过限制");
        StringBuilder tmpPath = new StringBuilder();

        int idx = writeDto.getPath().length() - 1;

        while(idx >= 0 && writeDto.getPath().charAt(idx) != '/') {
            idx--;
        }

        StringBuilder fileName = new StringBuilder();

        for (int i = idx + 1; i < writeDto.getPath().length(); i ++) {
            fileName.append(writeDto.getPath().charAt(i));
        }

        for (int i = 0; i < idx; i ++) {
            tmpPath.append(writeDto.getPath().charAt(i));
        }
        if(tmpPath.isEmpty()) tmpPath.append("/");

        R<String> tmp = fileSystemService.checkDir(new ChangePathDto(tmpPath.toString(), writeDto.getUsername(), writeDto.getGroup()), fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String path = tmp.getData();

        return fileSystemService.writeFile(fileSystem, path, writeDto.getGroup(), fileName.toString(), writeDto.getData());
    }

    /**
     * 获取/sys/del的post请求，请求体为文件路径，用户名，用户组，先将路径分隔为路径以及文件名，之后调用服务层的checkout检测能否进入目的目录，之后调用服务层的delFile删除文件。
     * @param delDto
     * @return
     */
    @ApiOperation("删除文件")
    @PostMapping("/del")
    public R<String> delFile(@RequestBody DelDto delDto) {
        StringBuilder tmpPath = new StringBuilder();

        int idx = delDto.getPath().length() - 1;

        while(idx >= 0 && delDto.getPath().charAt(idx) != '/') {
            idx--;
        }

        StringBuilder fileName = new StringBuilder();

        for (int i = idx + 1; i < delDto.getPath().length(); i ++) {
            fileName.append(delDto.getPath().charAt(i));
        }

        for (int i = 0; i < idx; i ++) {
            tmpPath.append(delDto.getPath().charAt(i));
        }
        if(tmpPath.isEmpty()) tmpPath.append("/");

        R<String> tmp = fileSystemService.checkDir(new ChangePathDto(tmpPath.toString(), delDto.getUsername(), delDto.getGroup()), fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String path = tmp.getData();

        R<String> ret = fileSystemService.delFile(fileSystem, path, delDto.getUsername(), fileName.toString());
        if (ret.getCode() == 0) return ret;

        if (!path.equals("/")) path += "/";
        path += fileName;

        fileSystem = fileSystemService.removeFromParent(fileSystem, path);

        return ret;
    }

    /**
     * 负责接受/sys/copy的post请求，请求体包括复制的源路径以及目标路径，用户名以及用户组。先通过分割/将源路径以及目标路径的目录路径和目录名或者文件名分割开，调用服务层的checkDir检测用户能否进入源路径，之后调用服务层的checkFileGroup检测目录下是否有无法读取的文件，之后检测用户能否进入到目标文件路径，最后调用服务层的copyFileInside完成复制。
     * @param copyDto
     * @return
     */
    @ApiOperation("文件系统内部复制")
    @PostMapping("/copy")
    public R<String> copyFile(@RequestBody CopyDto copyDto) {
        //拿到原来的路径
        StringBuilder tmpFromPath = new StringBuilder();

        int idx = copyDto.getFromPath().length() - 1;

        while(idx >= 0 && copyDto.getFromPath().charAt(idx) != '/') {
            idx--;
        }

        StringBuilder fileFromName = new StringBuilder();

        for (int i = idx + 1; i < copyDto.getFromPath().length(); i ++) {
            fileFromName.append(copyDto.getFromPath().charAt(i));
        }

        for (int i = 0; i < idx; i ++) {
            tmpFromPath.append(copyDto.getFromPath().charAt(i));
        }
        if(tmpFromPath.isEmpty()) tmpFromPath.append("/");

        R<String> tmp = fileSystemService.checkDir(new ChangePathDto(tmpFromPath.toString(), copyDto.getUsername(), copyDto.getGroup()), fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String fromPath = tmp.getData();

        //检查目录文件下是否有其他组的文件
        tmp = fileSystemService.checkFileGroup(fileSystem, fromPath, fileFromName.toString(), copyDto.getGroup());
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }

        //拿到要复制的路径
        StringBuilder tmpToPath = new StringBuilder();

        idx = copyDto.getToPath().length() - 1;

        while(idx >= 0 && copyDto.getToPath().charAt(idx) != '/') {
            idx--;
        }

        StringBuilder fileToName = new StringBuilder();

        for (int i = idx + 1; i < copyDto.getToPath().length(); i ++) {
            fileToName.append(copyDto.getToPath().charAt(i));
        }

        for (int i = 0; i < idx; i ++) {
            tmpToPath.append(copyDto.getToPath().charAt(i));
        }
        if (tmpToPath.isEmpty()) tmpToPath.append("/");

        tmp = fileSystemService.checkDir(new ChangePathDto(tmpToPath.toString(), copyDto.getUsername(), copyDto.getGroup()), fileSystem);
        if (tmp.getCode() == 0) {
            return R.error(tmp.getMsg());
        }
        String toPath = tmp.getData();

        return fileSystemService.copyFileInside(fileSystem, fromPath, toPath, fileFromName.toString(), fileToName.toString(), copyDto.getUsername());
    }

    /**
     * 获取/sys/simdisk的post请求，请求体包括源路径、目标路径以及用户名，用户组，首先将源路径源文件名以及目标路径和目标文件名分割出来，如果源路径时unix文件系统内部，则调用服务层的checkDir层以及checkFileGroup检测是否有权限读文件，否则检测本地系统是否存在，目标路径同理，如果是内部系统的复制则调用服务层的copyFileInside接口，否则调用simdiskCopy接口。
     * @param copyDto
     * @return
     */
    @SneakyThrows
    @ApiOperation("host文件系统与本地文件系统复制")
    @PostMapping("/simdisk")
    public R<String> simdiskCopy(@RequestBody CopyDto copyDto) {
        //拿到原来的路径
        String fromPath = "";
        String fromHost = "";
        StringBuilder fileFromName = new StringBuilder();
        if (copyDto.getFromPath().charAt(0) == '/') {
            StringBuilder tmpFromPath = new StringBuilder();

            int idx = copyDto.getFromPath().length() - 1;

            while (idx >= 0 && copyDto.getFromPath().charAt(idx) != '/') {
                idx--;
            }


            for (int i = idx + 1; i < copyDto.getFromPath().length(); i++) {
                fileFromName.append(copyDto.getFromPath().charAt(i));
            }

            for (int i = 0; i < idx; i++) {
                tmpFromPath.append(copyDto.getFromPath().charAt(i));
            }
            if (tmpFromPath.isEmpty()) tmpFromPath.append("/");

            R<String> tmp = fileSystemService.checkDir(new ChangePathDto(tmpFromPath.toString(), copyDto.getUsername(), copyDto.getGroup()), fileSystem);
            if (tmp.getCode() == 0) {
                return R.error(tmp.getMsg());
            }
            fromPath = tmp.getData();

            //检查目录文件下是否有其他组的文件
            tmp = fileSystemService.checkFileGroup(fileSystem, fromPath, fileFromName.toString(), copyDto.getGroup());
            if (tmp.getCode() == 0) {
                return R.error(tmp.getMsg());
            }


        }else {
            if (copyDto.getFromPath().charAt(0) != '<') return R.error("路径错误，无host");
            int idx = 1;
            for (; idx < copyDto.getFromPath().length() && copyDto.getFromPath().charAt(idx) != '>'; idx++) {
                fromHost += copyDto.getFromPath().charAt(idx);
            }
            if (idx == copyDto.getFromPath().length()) return R.error("路径错误");
            for (idx = idx + 1; idx < copyDto.getFromPath().length(); idx ++) {
                fromPath += copyDto.getFromPath().charAt(idx);
                if (copyDto.getFromPath().charAt(idx) == '\\') fromPath += '\\';
            }
            fromPath = HostPath + fromHost + "\\" + fromPath;

            idx = copyDto.getFromPath().length() - 1;
            while (idx >= 0 && copyDto.getFromPath().charAt(idx) != '\\') idx--;
            if (idx == -1) return R.error("路径错误");
            for (idx = idx + 1; idx < copyDto.getFromPath().length(); idx ++) {
                fileFromName.append(copyDto.getFromPath().charAt(idx));
            }
        }


        String toPath = "";
        String toHost = "";
        StringBuilder fileToName = new StringBuilder();

        if (copyDto.getToPath().charAt(0) == '/') {
            //拿到要复制的路径
            StringBuilder tmpToPath = new StringBuilder();

            int idx = copyDto.getToPath().length() - 1;

            while (idx >= 0 && copyDto.getToPath().charAt(idx) != '/') {
                idx--;
            }


            for (int i = idx + 1; i < copyDto.getToPath().length(); i++) {
                fileToName.append(copyDto.getToPath().charAt(i));
            }

            for (int i = 0; i < idx; i++) {
                tmpToPath.append(copyDto.getToPath().charAt(i));
            }
            if (tmpToPath.isEmpty()) tmpToPath.append("/");

            R<String> tmp = fileSystemService.checkDir(new ChangePathDto(tmpToPath.toString(), copyDto.getUsername(), copyDto.getGroup()), fileSystem);
            if (tmp.getCode() == 0) {
                return R.error(tmp.getMsg());
            }
            toPath = tmp.getData();
        }
        else {
            if (copyDto.getToPath().charAt(0) != '<') return R.error("路径错误，无host");
            int idx = 1;
            for (; idx < copyDto.getToPath().length() && copyDto.getToPath().charAt(idx) != '>'; idx++) {
                toHost += copyDto.getToPath().charAt(idx);
            }
            if (idx == copyDto.getToPath().length()) return R.error("路径错误");
            for (idx = idx + 1; idx < copyDto.getToPath().length(); idx ++) {
                toPath += copyDto.getToPath().charAt(idx);
                if (copyDto.getToPath().charAt(idx) == '\\') toPath += '\\';
            }
            toPath = HostPath + toHost + "\\" + toPath;

            idx = copyDto.getToPath().length() - 1;
            while (idx >= 0 && copyDto.getToPath().charAt(idx) != '\\') idx--;
            if (idx == -1) return R.error("路径错误");
            for (idx = idx + 1; idx < copyDto.getToPath().length(); idx ++) {
                fileToName.append(copyDto.getToPath().charAt(idx));
            }
        }

        if (fromHost.equals("") && toHost.equals("")) {
            return fileSystemService.copyFileInside(fileSystem, fromPath, toPath, fileFromName.toString(), fileToName.toString(), copyDto.getUsername());
        }
        return fileSystemService.simdiskCopy(fileSystem, fromPath, toPath,fileFromName.toString(), fileToName.toString(), copyDto.getUsername());
    }

}
