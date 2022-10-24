export default {
    // 开启命名空间
    namespaced: true,
    state: {
      info: {
        username: 'username',
        group: 'group',
        curPath: '/',
      }
    },
    mutations: {
      updateUsername(state, val) {
        state.info.username = val
      },
      updateGroup(state, val) {
        state.info.group = val
      },
      updatePath(state, val) {
        state.info.curPath = val;
      }
    },
    actions: {
      
    },
    getters: {
      format(state) {
        return state.info.username + ", " + state.info.group + ", " + state.info.curPath;
      }
    }
  }
  