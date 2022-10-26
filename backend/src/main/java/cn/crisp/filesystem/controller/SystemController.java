package cn.crisp.filesystem.controller;

import cn.crisp.filesystem.common.R;
import cn.crisp.filesystem.dto.ChangePathDto;
import cn.crisp.filesystem.dto.GroupDto;
import cn.crisp.filesystem.dto.LoginDto;
import cn.crisp.filesystem.entity.DirTree;
import cn.crisp.filesystem.entity.User;
import cn.crisp.filesystem.service.FileSystemService;
import cn.crisp.filesystem.system.FileSystem;
import cn.crisp.filesystem.vo.FileVo;
import cn.crisp.filesystem.vo.HelpVo;
import cn.crisp.filesystem.vo.UserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
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
}
