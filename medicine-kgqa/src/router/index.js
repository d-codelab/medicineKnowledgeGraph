import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '@/views/Login'
import Register from '@/views/Register'
import Reset from '@/views/Reset'
import HelloHome from '@/views/HelloHome'
import ZhiNengque from '@/views/ZhiNengque'
import articles from '@/views/articles'
import ArticleDetail from '@/views/ArticleDetail'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    component: Login,
    redirect: '/login',
    hidden: true,
    children: [
      {
        path: 'login',
        name: 'Login',
        component: Login
      }
    ]
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/reset',
    name: 'Reset',
    component: Reset
  },
  {
    path: '/home',
    name: 'HelloHome',
    component: HelloHome
  },
  {
    path: '/que',
    name: 'ZhiNengque',
    component: ZhiNengque

  },
  {
    path: '/articles',
    name: 'articles',
    component: articles

  },
  {
    path: '/2dView',
    name: '2dView',
    component: () => import(/* webpackChunkName: "about" */ '../views/2dView.vue')
  },
  {
    path: '/3dView',
    name: '3dView',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ '../views/3dView.vue')
  },
  {
    path: '/articleDetail/:id',
    name: 'ArticleDetail',
    component: ArticleDetail
  },
]

const router = new VueRouter({
  routes
})

export default router
