export default [
  {
    path: '/project',
    meta: { requireLogin: true },
    component: () => import(/* webpackChunkName: 'root' */ '@/views/home'),
    children: [
      {
        path: 'my',
        meta: { requireLogin: true },
        component: () => import(/* webpackChunkName: 'root' */ '@/views/project/my/index')
      },
      {
        path: 'recycle',
        meta: { requireLogin: true },
        component: () => import(/* webpackChunkName: 'root' */ '@/views/project/recycle/index')
      },
      {
        path: 'template',
        meta: { requireLogin: true },
        component: () => import(/* webpackChunkName: 'root' */ '@/views/project/template/index')
      },
      {
        path: 'template/category',
        meta: { requireLogin: true },
        component: () => import(/* webpackChunkName: 'root' */ '@/views/project/template/category.vue')
      },
      {
        path: 'theme/category',
        meta: { requireLogin: true },
        component: () => import(/* webpackChunkName: 'root' */ '@/views/project/theme/category.vue')
      },
      {
        path: 'theme/index',
        meta: { requireLogin: true },
        component: () => import(/* webpackChunkName: 'root' */ '@/views/project/theme/index.vue')
      },
      {
        path: 'template/preview',
        meta: { requireLogin: true },
        component: () => import(/* webpackChunkName: 'root' */ '@/views/project/template/preview.vue')
      },
      {
        path: 'square',
        meta: { requireLogin: false },
        component: () => import(/* webpackChunkName: 'root' */ '@/views/project/square/index')
      }
    ]
  },
  {
    path: '/project/form/view',
    meta: { requireLogin: false },
    component: () => import(/* webpackChunkName: 'root' */ '@/views/form/preview/ProjectForm.vue')
  },
  {
    path: '/project/public/result',
    meta: { requireLogin: false },
    component: () => import(/* webpackChunkName: 'root' */ '@/views/form/statistics/public')
  },
  {
    path: '/s/:key',
    meta: { requireLogin: false },
    component: () => import(/* webpackChunkName: 'root' */ '@/views/form/write'),
    props: (route) => ({ isShared: true })
  },
  {
    path: '/form/:key',
    meta: { requireLogin: true },
    component: () => import(/* webpackChunkName: 'root' */ '@/views/form/write'),
    props: (route) => ({ isShared: false })
  },
  {
    path: '/project/write',
    meta: { requireLogin: false },
    component: () => import(/* webpackChunkName: 'root' */ '@/views/form/write')
  }
]
