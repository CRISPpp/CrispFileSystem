import { createRouter, createWebHashHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import IndexView from '@/views/IndexView.vue'
const routes = [
  {
    path:'/',
    name:'login',
    component:LoginView,
  },
  {
    path:'/index',
    name:'index',
    component: IndexView,
     
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
