<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ChatLineSquare,
  DataAnalysis,
  HomeFilled,
  Setting,
  SwitchButton,
  Tickets,
  UserFilled,
} from '@element-plus/icons-vue';
import { clearAuthState, getCurrentUser } from '@/utils/auth';

const route = useRoute();
const router = useRouter();
const currentUser = getCurrentUser();

const menuItems = [
  { index: '/admin/dashboard', title: '数据看板', icon: DataAnalysis },
  { index: '/admin/houses', title: '房源管理', icon: HomeFilled },
  { index: '/admin/orders', title: '订单管理', icon: Tickets },
  { index: '/admin/reviews', title: '评价管理', icon: ChatLineSquare },
  { index: '/admin/users', title: '用户管理', icon: UserFilled },
  { index: '/admin/settings', title: '系统配置', icon: Setting },
];

const pageTitle = computed(() => {
  const item = menuItems.find((m) => route.path.startsWith(m.index));
  return item?.title ?? '管理后台';
});

const breadcrumbs = computed(() => {
  const parts: string[] = ['管理后台'];
  const item = menuItems.find((m) => route.path.startsWith(m.index));
  if (item) parts.push(item.title);
  if (route.path.includes('/calendar/')) parts.push('房态维护');
  return parts;
});

function logout() {
  clearAuthState();
  router.push('/login');
}
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-brand" @click="router.push('/admin/dashboard')">
        <div class="brand-icon">
          <svg viewBox="0 0 36 36" fill="none">
            <rect width="36" height="36" rx="9" fill="url(#adminGrad)" />
            <path d="M10 26V14l8 5.5L26 14v12" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <rect x="16" y="18" width="4" height="8" rx="1" fill="var(--c-coral)" opacity="0.85" />
            <defs>
              <linearGradient id="adminGrad" x1="0" y1="0" x2="36" y2="36">
                <stop stop-color="#0ea5e9" />
                <stop offset="1" stop-color="#0284c7" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div class="brand-text">
          <div class="brand-title">栖云智宿</div>
          <div class="brand-sub">SmartStay Console</div>
        </div>
      </div>

      <el-menu :default-active="route.path" class="admin-menu" router>
        <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-bottom">
        <div class="logout-user">
          <el-avatar :size="34" class="user-avatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="user-info">
            <strong>{{ currentUser?.name || currentUser?.username || '管理员' }}</strong>
            <span>系统管理员</span>
          </div>
        </div>
        <el-button :icon="SwitchButton" text class="logout-btn" @click="logout">
          退出登录
        </el-button>
      </div>
    </aside>

    <section class="admin-main">
      <header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(b, i) in breadcrumbs" :key="i">{{ b }}</el-breadcrumb-item>
          </el-breadcrumb>
          <h2><span class="title-accent"></span>{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <span class="header-user">{{ currentUser?.name || currentUser?.username }}</span>
          <el-button text type="info" @click="logout">退出</el-button>
        </div>
      </header>

      <div class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </section>
  </div>
</template>

<style scoped>
.admin-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  min-height: 100vh;
  background: var(--c-bg);
}

/* Sidebar */
.admin-sidebar {
  position: sticky;
  top: 0;
  display: flex;
  height: 100vh;
  padding: 20px 12px;
  flex-direction: column;
  background: linear-gradient(175deg, #0f1a2b 0%, #1a293b 50%, #1e2d42 100%);
  border-right: 1px solid rgb(255 255 255 / 5%);
}

/* Brand */
.sidebar-brand {
  display: flex; align-items: center; gap: 10px;
  padding: 6px 10px 16px;
  border-bottom: 1px solid rgb(255 255 255 / 8%);
  cursor: pointer;
  transition: opacity var(--transition-fast);
}
.sidebar-brand:hover { opacity: 0.85; }

.brand-icon { width: 34px; height: 34px; flex-shrink: 0; }
.brand-icon svg {
  width: 100%; height: 100%;
  filter: drop-shadow(0 2px 8px rgb(2 132 199 / 38%));
}

.brand-title { color: #f8fafc; font-size: 16px; font-weight: 700; }
.brand-sub {
  color: #94a3b8; font-size: 10px; font-weight: 500;
  letter-spacing: 0.8px; text-transform: uppercase;
}

/* User */
.user-avatar {
  background: linear-gradient(135deg, #0ea5e9, var(--c-primary-dark));
  color: #fff;
  box-shadow: 0 2px 8px rgb(2 132 199 / 28%);
}
.user-info { display: flex; min-width: 0; flex-direction: column; gap: 2px; }
.user-info strong { color: #f1f5f9; font-size: 14px; font-weight: 600; }
.user-info span { color: #94a3b8; font-size: 12px; }

/* Menu */
.admin-menu {
  flex: 1; min-height: 0; overflow-y: auto;
  background: transparent; border-right: 0; padding-top: 8px;
}
.admin-menu :deep(.el-menu-item) {
  height: 44px; margin-bottom: 4px;
  padding-left: 14px !important;
  color: #cbd5e1; font-size: 14px; font-weight: 500;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}
.admin-menu :deep(.el-menu-item .el-icon) { font-size: 18px; margin-right: 10px; }
.admin-menu :deep(.el-menu-item:hover) {
  color: #fff; background: rgb(255 255 255 / 8%);
}
.admin-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(135deg, rgb(2 132 199 / 25%), rgb(2 132 199 / 12%));
  box-shadow:
    0 0 0 1px rgb(2 132 199 / 22%),
    inset 3px 0 0 var(--c-primary);
  font-weight: 600;
}

/* Bottom */
.sidebar-bottom {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid rgb(255 255 255 / 6%);
}
.logout-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 8px 2px;
}
.logout-btn {
  width: 100%; justify-content: center;
  color: rgb(248 113 113 / 85%);
  font-weight: 500; border-radius: var(--radius-md);
  transition: all var(--transition-base);
}
.logout-btn:hover {
  color: #f87171;
  background: rgb(239 68 68 / 12%);
}

/* Main */
.admin-main { min-width: 0; padding: 24px 28px; }

.admin-header {
  display: flex; align-items: center; justify-content: space-between;
  gap: 16px; margin-bottom: 22px;
  padding: 18px 24px;
  background: linear-gradient(135deg, var(--c-surface) 0%, var(--c-bg-warm) 50%, var(--c-surface) 100%);
  border-radius: var(--radius-lg);
  border: 1px solid rgb(229 232 236 / 50%);
}
.admin-header h2 {
  margin: 6px 0 0;
  font-size: 22px; font-weight: 700;
  letter-spacing: -0.5px; color: var(--c-ink);
  display: flex; align-items: center; gap: 10px;
}

.title-accent {
  display: inline-block;
  width: 4px; height: 22px;
  background: linear-gradient(180deg, var(--c-primary), var(--c-primary-light));
  border-radius: var(--radius-full);
}

.header-right {
  display: flex; align-items: center; gap: 16px; flex-shrink: 0;
}
.header-user { font-size: 14px; color: var(--c-body); }
.admin-content { min-width: 0; }

/* Transitions */
.fade-enter-active,
.fade-leave-active { transition: all 0.25s ease; }
.fade-enter-from { opacity: 0; transform: translateX(-8px); }
.fade-leave-to   { opacity: 0; transform: translateX(8px); }

/* Responsive */
@media (max-width: 860px) {
  .admin-layout { grid-template-columns: 1fr; }
  .admin-sidebar {
    position: relative; height: auto; padding: 10px;
    flex-direction: row; flex-wrap: wrap; gap: 6px;
  }
  .sidebar-brand, .sidebar-bottom { display: none; }
  .admin-menu {
    display: flex; flex: 1 1 auto; min-width: 0; gap: 4px; padding-top: 0;
  }
  .admin-menu :deep(.el-menu-item) {
    flex: 1; justify-content: center; padding: 0 12px !important; margin-bottom: 0;
  }
  .admin-menu :deep(.el-menu-item .el-icon) { margin-right: 6px; }
  .admin-main { padding: 14px 16px; }
  .admin-header {
    flex-direction: column; align-items: stretch; gap: 10px;
    padding: 14px 16px;
  }
}
</style>
