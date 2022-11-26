const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 80,
    proxy: {
      '/api':{
        target: 'http://127.0.0.1:8888',
        ws: true,
        changeOrigin: true,
        pathRewrite: {
          '^/api':'',
        }
      }
    }
  }
})
