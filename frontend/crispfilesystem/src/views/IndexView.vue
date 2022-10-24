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
        <div class="file_view">
            文件视图
        </div>
        <div class="cmd">
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
        });

        let logout = () => {
            router.push({ path: '/' });
        };

        let handleCMD = () => {
            if (cmd.value == "help") {
                getHelp();
            }
            cmd.value = "";
        }

        let getHelp = () => {
            axios.get('/api/sys/help').then((response) => {
                if (response.data.code === 1) {
                    cmd_context.context.unshift(" ");
                    for (let i = response.data.data.cmdlist.length - 1; i >= 0; i--) {
                        let str = response.data.data.cmdlist[i];
                        str += ': ';
                        str += response.data.data.cmddescription[i];
                        cmd_context.context.unshift(str);
                    }
                    cmd_context.context.unshift(store.state.user.info.curPath + "/help");
                }
                else {
                    ElNotification({
                        title: '发生错误,请稍后重试',
                        message: response.data.msg,
                        type: 'error',
                    })
                }
            }).catch(error => console.log(error));

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

.file_view {
    height: 40vh;
    background-color: blanchedalmond;
    opacity: 0.8;
}

.cmd {
    height: 43vh;
    background-color: bisque;
    opacity: 0.8;
}

.cmd_his {
    height: 35vh;
    width: 100vw;
    background-color: black;
    overflow: auto;
}

.cmd_his_context {
    color: white;
    height: 2vh;
}

.cmd_input {
    bottom: 0;
    position: absolute;
}

.cmd_input_path {
    display: inline-block;
}

.cmd_input_text {
    display: inline-block;
    width: 30vw;
}
</style>