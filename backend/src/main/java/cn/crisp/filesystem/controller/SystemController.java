package cn.crisp.filesystem.controller;

import cn.crisp.filesystem.common.R;
import cn.crisp.filesystem.dto.LoginDto;
import cn.crisp.filesystem.entity.User;
import cn.crisp.filesystem.service.FileSystemService;
import cn.crisp.filesystem.system.FileSystem;
import cn.crisp.filesystem.vo.HelpVo;
import cn.crisp.filesystem.vo.UserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @GetMapping("/test")
    public R<FileSystem> test() {
        fileSystem = fileSystemService.test(fileSystem);
        return R.success(fileSystem);
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
}
