export default [
  // user
  {
    path: '/user',
    component: '../layouts/UserLayout',
    routes: [
      { path: '/user', redirect: '/user/login' },
      { path: '/user/login', component: './User/Login' },
      { path: '/user/register', component: './User/Register' },
      { path: '/user/register-result', component: './User/RegisterResult' },
    ],
  },
  // app
  {
    path: '/',
    component: '../layouts/BasicLayout',
    Routes: ['src/pages/Authorized'],
    authority: ['admin', 'user'],
    routes: [
      // dashboard
      { path: '/', redirect: '/task-list' },
      {
        path: '/config-group-list',
        icon: 'setting',
        name: 'configgrouplist',
        component: './config/ConfigGroupList',
      },
      {
        path: '/task-list',
        icon: 'project',
        name: 'tasklist',
        component: './like/TaskList',
      },
      {
        component: '404',
      },
    ],
  },
];
