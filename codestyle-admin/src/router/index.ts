import { createRouter, createWebHistory } from 'vue-router';
import NProgress from 'nprogress'; // progress bar
import 'nprogress/nprogress.css';

import { appRoutes } from './routes';
import { NOT_FOUND_ROUTE } from './routes/base';

NProgress.configure({ showSpinner: false }); // NProgress Configuration

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 官网首页
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home/index.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register/index.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/explore',
      name: 'explore',
      component: () => import('@/views/explore/index.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/catalog',
      name: 'catalog',
      component: () => import('@/views/catalog/index.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/search/index.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/submit',
      name: 'submit',
      component: () => import('@/views/submit/index.vue'),
      meta: { requiresAuth: false },
    },
    ...appRoutes,
    NOT_FOUND_ROUTE,
  ],
  scrollBehavior() {
    return { top: 0 };
  },
});

// 已精简为纯官网，无需复杂路由守卫

export default router;
