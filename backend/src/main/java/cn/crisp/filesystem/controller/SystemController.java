package cn.crisp.filesystem.controller;

import cn.crisp.filesystem.common.R;
import cn.crisp.filesystem.dto.ChangePathDto;
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


    @ApiOperation("改变目录")
    @PostMapping("/cd")
    public R<String> cd(@RequestBody ChangePathDto changePathDto) {
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

    @ApiOperation("查询目录内容")
    @GetMapping("dir")
    public R<List<FileVo>> dir(@RequestParam String path) {
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

        return R.success(ret);
    }
}
