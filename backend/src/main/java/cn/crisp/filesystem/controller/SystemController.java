package cn.crisp.filesystem.controller;

import cn.crisp.filesystem.common.R;
import cn.crisp.filesystem.dto.*;
import cn.crisp.filesystem.entity.DirTree;
import cn.crisp.filesystem.entity.User;
import cn.crisp.filesystem.service.FileSystemService;
import cn.crisp.filesystem.system.FileSystem;
import cn.crisp.filesystem.vo.FileVo;
import cn.crisp.filesystem.vo.HelpVo;
import cn.crisp.filesystem.vo.UserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static cn.crisp.filesystem.common.Constants.CMDDescription;
import static cn.crisp.filesystem.common.Constants.CMDList;

@Api("CrispFileSystem")
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
        fileSystem = fileSystemService.check(fileSystem);
        return R.success("读取成功");
    }

    @ApiOperation("保存文件系统")
    @PostMapping("/save")
    public R<String> save(@RequestBody GroupDto groupDto) {
        if (!"root".equals(groupDto.getGroup())) {
            return R.error("没有保存权限");
        }
        fileSystemService.save(fileSystem);
        return R.success("保存成功");
    }


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


    @ApiOperation("写入文件")
    @PostMapping("/writeFile")
    public R<String> writeFile(@RequestBody WriteDto writeDto) {
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
}
