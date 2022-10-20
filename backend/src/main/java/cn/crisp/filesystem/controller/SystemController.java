package cn.crisp.filesystem.controller;

import cn.crisp.filesystem.common.R;
import cn.crisp.filesystem.dto.LoginDto;
import cn.crisp.filesystem.entity.Fcb;
import cn.crisp.filesystem.service.FileSystemService;
import cn.crisp.filesystem.system.FileSystem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Api("CrispFileSystem")
@RequestMapping("/sys")
@RestController
public class SystemController {
    @Autowired
    FileSystem fileSystem;



    @Autowired
    FileSystemService fileSystemService;

    @GetMapping("/test")
    public R<String> test() {
        fileSystem = fileSystemService.test(fileSystem);
        return R.success(fileSystem.getBootBlock().toString());
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R<String> login(@RequestBody LoginDto loginDto){
        if (!fileSystem.getBootBlock().getBootInfo().containsKey(loginDto.getUsername())) {
            return R.error("用户名错误");
        }

        if (!Objects.equals(fileSystem.getBootBlock().getBootInfo().get(loginDto.getUsername()), loginDto.getPassword())) {
            return R.error("密码错误");
        }

        return R.success("登录成功");
    }

}
