import { createRouter, createWebHistory } from 'vue-router';
import PortalLayout from '@/layouts/PortalLayout.vue';
import AdminLayout from '@/layouts/AdminLayout.vue';
import BlankLayout from '@/layouts/BlankLayout.vue';
import { defaultPathForRole, getCurrentUser, hasRoutePermission } from '@/utils/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 用户端门户
    {
      path: '/portal',
      component: PortalLayout,
      redirect: '/portal/home',
      children: [
        {
          path: 'home',
          name: 'PortalHome',
          component: () => import('@/views/portal/Home.vue'),
          meta: { title: '首页' },
        },
        {
          path: 'houses',
          name: 'PortalHouseList',
          component: () => import('@/views/portal/HouseList.vue'),
          meta: { title: '房源列表' },
        },
        {
          path: 'houses/:id',
          name: 'PortalHouseDetail',
          component: () => import('@/views/portal/HouseDetail.vue'),
          meta: { title: '房源详情' },
        },
        {
          path: 'booking/:houseId',
          name: 'PortalBooking',
          component: () => import('@/views/portal/Booking.vue'),
          meta: { title: '预约下单', requiresAuth: true },
        },
        {
          path: 'orders',
          name: 'PortalMyOrders',
          component: () => import('@/views/portal/MyOrders.vue'),
          meta: { title: '我的订单', requiresAuth: true },
        },
        {
          path: 'orders/:orderNo',
          name: 'PortalOrderDetail',
          component: () => import('@/views/portal/OrderDetail.vue'),
          meta: { title: '订单详情', requiresAuth: true },
        },
        {
          path: 'review/:orderNo',
          name: 'PortalReviewCreate',
          component: () => import('@/views/portal/ReviewCreate.vue'),
          meta: { title: '发表评价', requiresAuth: true },
        },
        {
          path: 'favorites',
          name: 'PortalFavorites',
          component: () => import('@/views/portal/Favorites.vue'),
          meta: { title: '我的收藏', requiresAuth: true },
        },
        {
          path: 'profile',
          name: 'PortalProfile',
          component: () => import('@/views/portal/Profile.vue'),
          meta: { title: '个人中心', requiresAuth: true },
        },
      ],
    },
    // 管理后台
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '数据看板', icon: 'DataAnalysis' },
        },
        {
          path: 'houses',
          name: 'AdminHouseManage',
          component: () => import('@/views/admin/HouseManage.vue'),
          meta: { title: '房源管理', icon: 'HomeFilled' },
        },
        {
          path: 'calendar/:houseId',
          name: 'AdminCalendarManage',
          component: () => import('@/views/admin/CalendarManage.vue'),
          meta: { title: '房态维护', icon: 'Calendar' },
        },
        {
          path: 'orders',
          name: 'AdminOrderManage',
          component: () => import('@/views/admin/OrderManage.vue'),
          meta: { title: '订单管理', icon: 'Tickets' },
        },
        {
          path: 'reviews',
          name: 'AdminReviewManage',
          component: () => import('@/views/admin/ReviewManage.vue'),
          meta: { title: '评价管理', icon: 'ChatLineSquare' },
        },
        {
          path: 'users',
          name: 'AdminUserManage',
          component: () => import('@/views/admin/UserManage.vue'),
          meta: { title: '用户管理', icon: 'UserFilled' },
        },
        {
          path: 'settings',
          name: 'AdminSettings',
          component: () => import('@/views/admin/Settings.vue'),
          meta: { title: '系统配置', icon: 'Setting' },
        },
      ],
    },
    // 公共页面
    {
      path: '/login',
      component: BlankLayout,
      children: [
        {
          path: '',
          name: 'Login',
          component: () => import('@/views/common/Login.vue'),
          meta: { title: '登录' },
        },
      ],
    },
    {
      path: '/',
      redirect: '/portal/home',
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/portal/home',
    },
  ],
});

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 栖云智宿` : '栖云智宿';

  const currentUser = getCurrentUser();

  if (to.path === '/login' && currentUser) {
    next(defaultPathForRole(currentUser.role));
    return;
  }

  if (to.meta.requiresAuth && !currentUser) {
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }

  if (currentUser && !hasRoutePermission(to.path, currentUser.role)) {
    next(defaultPathForRole(currentUser.role));
    return;
  }

  next();
});

export default router;
