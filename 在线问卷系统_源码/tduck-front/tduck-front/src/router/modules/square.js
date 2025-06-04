export default [
  {
    path: '/square',
    meta: { requireLogin: false },
    component: () => import(/* webpackChunkName: 'root' */ '@/views/project/square/index')
  }
]
