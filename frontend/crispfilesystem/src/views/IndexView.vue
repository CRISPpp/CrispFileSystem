<template>
    <div class="sys">
        <div class="top">
            <div class="top_tittle">
                CrispFileSystem
            </div>
            <div class="top_userinfo">
                <div class="tmp1">用户名: {{ store.state.user.info.username }}</div>
                <div class="tmp2">用户组: {{ store.state.user.info.group }}</div>
                <div class="tmp3">当前目录: {{ store.state.user.info.curPath }}</div>
            </div>
            <el-button type="primary" class="top_button" @click="logout">登出</el-button>
        </div>
        <div class="res_view">
            <div class="res_tittle">
                执行结果
            </div>
            <div class="res_his">
                <div class="cmd_res_context" v-for="(c, index) in cmd_context.res" :key="index">
                    {{ c }}</div>
            </div>
        </div>

        <div class="cmd">
            <div class="cmd_tittle">
                命令历史
            </div>
            <div class="cmd_his">
                <div class="cmd_his_context" v-for="(c, index) in cmd_context.context" :key="index">
                    {{ c }}</div>
            </div>
            <div class="cmd_input">
                <div class="cmd_input_path">{{ store.state.user.info.curPath }}</div>
                <div class="cmd_input_text">
                    <el-input v-model="cmd" placeholder="输入shell命令, 输入help查看命令大全" @keyup.enter="handleCMD" />
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { useStore } from 'vuex';
import router from '@/router';
import { reactive, ref } from 'vue';
import axios from 'axios';
import { ElNotification } from 'element-plus'

export default {
    name: "IndexView",
    setup() {
        let cmd = ref('');
        let store = useStore();
        let cmd_context = reactive({
            context: [],
            res: [],
        });

        let logout = () => {
            router.push({ path: '/' });
        };

        let handleCMD = () => {
            let mark = cmd.value.split(" ")
            if (mark[0] == "help") {
                getHelp();
            }

            else if (mark[0] == "info") {
                getInfo();
            }

            else if (mark[0] == "cd") {
                cd(mark[1]);
            }

            else if (mark[0] == "dir") {
                if (mark.length > 3) {
                    ElNotification({
                        title: '指令错误',
                        message: "请检查指令或者输入help查看指令信息",
                        type: 'error',
                    })
                } else if (mark.length === 2) {
                    dir(mark[1]);
                } else if (mark.length === 1) {
                    dir(store.state.user.info.curPath);
                }
            }

            else if (mark[0] == "check") {
                check();
            }

            else if (mark[0] == "save") {
                save();
            }

            else {
                ElNotification({
                    title: '指令错误',
                    message: "请检查指令或者输入help查看指令信息",
                    type: 'error',
                })
                cmd.value = "";
            }
        }

        let getHelp = () => {
            axios.get('/api/sys/help').then((response) => {
                if (response.data.code === 1) {
                    cmd_context.context.unshift(" ");
                    cmd_context.res.unshift(" ");
                    for (let i = response.data.data.cmdlist.length - 1; i >= 0; i--) {
                        let str = response.data.data.cmdlist[i];
                        str += ': ';
                        str += response.data.data.cmddescription[i];
                        cmd_context.res.unshift(str);
                    }
                    if (store.state.user.info.curPath == '/') {
                        cmd_context.context.unshift(store.state.user.info.curPath + 'help');
                    } else {
                        cmd_context.context.unshift(store.state.user.info.curPath + '/' + 'help');
                    }
                }
                else {
                    ElNotification({
                        title: '发生错误,请稍后重试',
                        message: response.data.msg,
                        type: 'error',
                    })
                }
            }).catch(error => console.log(error));
            cmd.value = "";
        }

        let getInfo = () => {
            axios.get('/api/sys/info').then((response) => {
                if (response.data.code === 1) {
                    cmd_context.context.unshift(" ");
                    cmd_context.res.unshift(" ");
                    for (let i = response.data.data.length - 1; i >= 0; i--) {
                        cmd_context.res.unshift(response.data.data[i]);
                    }
                    if (store.state.user.info.curPath == '/') {
                        cmd_context.context.unshift(store.state.user.info.curPath + 'info');
                    } else {
                        cmd_context.context.unshift(store.state.user.info.curPath + '/' + 'info');
                    }
                }
                else {
                    ElNotification({
                        title: '发生错误,请稍后重试',
                        message: response.data.msg,
                        type: 'error',
                    })
                }
            }).catch(error => console.log(error));
            cmd.value = "";
        }

        let cd = (path) => {
            if (path[0] != '/') {
                if (store.state.user.info.curPath == "/") {
                    path = store.state.user.info.curPath + path;
                } else {
                    path = store.state.user.info.curPath + '/' + path;
                }
            }
            axios.post('/api/sys/cd', {
                "path": path,
                "username": store.state.user.info.username,
                "group": store.state.user.info.group,
            }).then((response) => {
                if (response.data.code === 1) {
                    cmd_context.context.unshift(" ");
                    cmd_context.context.unshift("cd " + path);
                    path = response.data.data;
                    store.commit("user/updatePath", path);
                    cmd_context.res.unshift(" ");
                    cmd_context.res.unshift(path);
                }
                else {
                    ElNotification({
                        title: '发生错误',
                        message: response.data.msg,
                        type: 'error',
                    })
                }
            }).catch(error => console.log(error));
            cmd.value = "";
        }

        let formatFile = (file) => {
            if (file.isDir === 1) {
                cmd_context.res.unshift("目录在inode区的块号为: " + file.id);
                cmd_context.res.unshift("目录权限保护码: " + file.limit);
                cmd_context.res.unshift("目录创建时间: " + file.createTime);
                cmd_context.res.unshift("目录创建者: " + file.createBy);
                cmd_context.res.unshift("目录中文件个数: " + file.length);
                cmd_context.res.unshift("目录名: " + file.filename);
            } else {
                let pos = "";
                for (let i = 0; i < 10; i++) {
                    if (file.address[i] === -1) break;
                    if (i != 0) pos += ', ';
                    pos += file.address[i];
                }
                if (file.address[10] === -1) {
                    cmd_context.res.unshift("文件内容在磁盘区无间接索引");
                } else {
                    cmd_context.res.unshift("文件内容在磁盘区的间接索引块号为: " + file.address[10]);
                }
                cmd_context.res.unshift("文件内容在磁盘区的直接索引块号为: " + pos);
                cmd_context.res.unshift("文件在inode区的块号为: " + file.id);
                cmd_context.res.unshift("文件权限保护码: " + file.limit);
                cmd_context.res.unshift("文件创建时间: " + file.createTime);
                cmd_context.res.unshift("文件创建者: " + file.createBy);
                cmd_context.res.unshift("文件大小: " + file.length + "byte");
                cmd_context.res.unshift("文件名: " + file.filename);
            }
        }
        let dir = (path) => {
            if (path[0] != '/') {
                if (store.state.user.info.curPath == "/") {
                    path = store.state.user.info.curPath + path;
                } else {
                    path = store.state.user.info.curPath + '/' + path;
                }
            }
            axios.post('/api/sys/dir', {
                'path': path,
                'username': store.state.user.info.username,
                'group': store.state.user.info.group,
            }).then((response) => {
                if (response.data.code === 1) {
                    for (let i = response.data.data.length - 1; i >= 0; i--) {
                        cmd_context.res.unshift(" ");
                        formatFile(response.data.data[i]);
                    }
                    cmd_context.context.unshift(" ");
                    if (store.state.user.info.curPath == '/') {
                        cmd_context.context.unshift(store.state.user.info.curPath + 'dir ' + path);
                    } else {
                        cmd_context.context.unshift(store.state.user.info.curPath + '/' + 'dir ' + path);
                    }
                }
                else {
                    ElNotification({
                        title: '发生错误',
                        message: response.data.msg,
                        type: 'error',
                    })
                }
            }).catch(error => console.log(error));
            cmd.value = "";
        }

        let check = () => {
            axios.post('/api/sys/check', {
                'group': store.state.user.info.group,
            }).then((response) => {
                if (response.data.code === 1) {
                    cmd_context.res.unshift(" ");
                    cmd_context.res.unshift(response.data.data);
                    cmd_context.context.unshift(" ");
                    if (store.state.user.info.curPath == '/') {
                        cmd_context.context.unshift(store.state.user.info.curPath + 'check');
                    } else {
                        cmd_context.context.unshift(store.state.user.info.curPath + '/' + 'check');
                    }
                }
                else {
                    ElNotification({
                        title: '发生错误',
                        message: response.data.msg,
                        type: 'error',
                    })
                }
            }).catch(error => console.log(error));
            cmd.value = "";
        }
        let save = () => {
            axios.post('/api/sys/save', {
                'group': store.state.user.info.group,
            }).then((response) => {
                if (response.data.code === 1) {
                    cmd_context.res.unshift(" ");
                    cmd_context.res.unshift(response.data.data);
                    cmd_context.context.unshift(" ");
                    if (store.state.user.info.curPath == '/') {
                        cmd_context.context.unshift(store.state.user.info.curPath + 'save');
                    } else {
                        cmd_context.context.unshift(store.state.user.info.curPath + '/' + 'save');
                    }
                }
                else {
                    ElNotification({
                        title: '发生错误',
                        message: response.data.msg,
                        type: 'error',
                    })
                }
            }).catch(error => console.log(error));
            cmd.value = "";
        }


        return {
            store,
            logout,
            cmd,
            cmd_context,
            handleCMD,
        }
    }

}
</script>

<style scoped>
.sys {
    background-image: url(@/assets/img/tsk.jpg);
    background-attachment: fixed;
    background-repeat: no-repeat;
    background-size: cover;
    min-height: 200vh;
}

.top {
    height: 17vh;
    font-size: 30px;
    text-align: center;
    font-weight: bold;
    left: 0;
    top: 0;
}

.top_userinfo {
    font-size: 20px;
}

.res_view {
    margin-top: 2vh;
    height: 50vh;
    background-color: blanchedalmond;
    opacity: 0.8;
}



.cmd {
    margin-top: 3vh;
    height: 43vh;
    background-color: bisque;
    opacity: 0.8;
}

.cmd_tittle {
    font-size: 20px;
    text-align: center;
    font-weight: bold;
}

.res_tittle {
    font-size: 20px;
    text-align: center;
    font-weight: bold;
}

.cmd_his {
    left: 2vw;
    position: relative;
    height: 35vh;
    width: 95vw;
    background-color: black;
    overflow: auto;
}

.res_his {
    left: 2vw;
    position: relative;
    height: 45vh;
    width: 95vw;
    background-color: black;
    overflow: auto;
}

.cmd_his_context {
    color: white;
    height: 2vh;
}

.cmd_res_context {
    color: white;
    height: 2vh;
}

.cmd_input {
    left: 2vw;
    bottom: 0;
    position: fixed;
}

.cmd_input_path {
    display: inline-block;
}

.cmd_input_text {
    display: inline-block;
    width: 30vw;
}
</style>