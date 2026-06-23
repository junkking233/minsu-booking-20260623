<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowDown, User } from '@element-plus/icons-vue';
import { clearAuthState, getCurrentUser } from '@/utils/auth';

const route = useRoute();
const router = useRouter();
const currentUser = getCurrentUser();

const navItems = [
  { label: '首页', path: '/portal/home' },
  { label: '房源列表', path: '/portal/houses' },
  { label: '我的订单', path: '/portal/orders' },
  { label: '我的收藏', path: '/portal/favorites' },
];

const activeMenu = computed(() => {
  if (route.path.startsWith('/portal/home')) return '/portal/home';
  if (route.path.startsWith('/portal/houses')) return '/portal/houses';
  if (route.path.startsWith('/portal/orders')) return '/portal/orders';
  if (route.path.startsWith('/portal/favorites')) return '/portal/favorites';
  return '/portal/home';
});

function logout() {
  clearAuthState();
  router.push('/login');
}
</script>

<template>
  <div class="portal-layout">
    <!-- 顶部导航 -->
    <header class="portal-header">
      <div class="header-inner">
        <div class="brand" @click="router.push('/portal/home')">
          <div class="brand-mark">
            <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="40" height="40" rx="10" fill="url(#portalGrad)" />
              <path d="M12 28V14l8 7 8-7v14" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
              <defs>
                <linearGradient id="portalGrad" x1="0" y1="0" x2="40" y2="40">
                  <stop stop-color="#0ea5e9" />
                  <stop offset="1" stop-color="#2563eb" />
                </linearGradient>
              </defs>
            </svg>
          </div>
          <span class="brand-text">民宿预约</span>
        </div>

        <el-menu :default-active="activeMenu" mode="horizontal" class="portal-menu" router>
          <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">
            {{ item.label }}
          </el-menu-item>
        </el-menu>

        <div class="header-actions">
          <template v-if="currentUser">
            <el-dropdown trigger="click" @command="(cmd: string) => { if (cmd === 'profile') router.push('/portal/profile'); else if (cmd === 'logout') logout(); }">
              <span class="user-badge">
                <el-avatar :size="32" class="user-avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="user-name">{{ currentUser.name || currentUser.username }}</span>
                <el-icon class="user-arrow"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <el-button v-else type="primary" class="login-btn" @click="router.push('/login')">
            登录
          </el-button>
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="portal-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="portal-footer">
      <div class="footer-inner">
        <span>&copy; 2026 民宿预约管理系统</span>
      </div>
    </footer>

    <!-- 移动端底部 Tab 栏 -->
    <div class="mobile-tabs">
      <div
        v-for="item in navItems"
        :key="item.path"
        class="mobile-tab"
        :class="{ active: activeMenu === item.path }"
        @click="router.push(item.path)"
      >
        <span>{{ item.label }}</span>
      </div>
      <div
        class="mobile-tab"
        :class="{ active: route.path === '/portal/profile' }"
        @click="currentUser ? router.push('/portal/profile') : router.push('/login')"
      >
        <span>{{ currentUser ? '我的' : '登录' }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.portal-layout {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  background:
    radial-gradient(ellipse 80% 60% at 10% 0%, rgb(14 165 233 / 6%), transparent),
    radial-gradient(ellipse 60% 50% at 90% 100%, rgb(37 99 235 / 5%), transparent),
    var(--c-bg);
  padding-bottom: 70px;
}

/* Header */
.portal-header {
  position: sticky;
  z-index: 50;
  top: 0;
  background: rgb(255 255 255 / 85%);
  border-bottom: 1px solid var(--c-line);
  backdrop-filter: blur(20px) saturate(1.8);
  -webkit-backdrop-filter: blur(20px) saturate(1.8);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 28px;
  min-height: 64px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: opacity var(--transition-fast);
  flex-shrink: 0;
}

.brand:hover {
  opacity: 0.85;
}

.brand-mark {
  width: 36px;
  height: 36px;
}

.brand-mark svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 4px rgb(14 165 233 / 30%));
}

.brand-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--c-ink);
  letter-spacing: -0.5px;
}

.portal-menu {
  flex: 1 1 auto;
  min-width: 0;
  max-width: 520px;
  margin: 0 20px;
  border-bottom: 0;
  justify-content: center;
  background: transparent;
}

.portal-menu :deep(.el-menu-item) {
  height: 48px;
  font-size: 15px;
  font-weight: 500;
  color: var(--c-body);
  border-bottom-width: 2.5px;
}

.portal-menu :deep(.el-menu-item:hover) {
  color: var(--c-primary);
  background: transparent;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.user-badge:hover {
  background: var(--c-line-light);
}

.user-avatar {
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--c-ink-light);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-arrow {
  font-size: 12px;
  color: var(--c-muted);
}

.login-btn {
  font-weight: 600;
  border-radius: var(--radius-md);
  padding: 0 20px;
}

/* Main */
.portal-main {
  flex: 1;
  min-width: 0;
}

/* Footer */
.portal-footer {
  padding: 18px 28px;
  background: var(--c-surface);
  border-top: 1px solid var(--c-line);
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  text-align: center;
  color: var(--c-muted);
  font-size: 13px;
}

/* Page transition */
.page-enter-active,
.page-leave-active {
  transition: all 0.25s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* Mobile tabs */
.mobile-tabs {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: var(--c-surface);
  border-top: 1px solid var(--c-line);
  padding: 6px 0 env(safe-area-inset-bottom);
}

.mobile-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  padding: 6px 0;
  font-size: 11px;
  color: var(--c-muted);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.mobile-tab.active {
  color: var(--c-primary);
  font-weight: 600;
}

@media (max-width: 768px) {
  .header-inner {
    padding: 10px 16px;
    min-height: 52px;
  }

  .brand-text {
    font-size: 17px;
  }

  .portal-menu {
    display: none;
  }

  .portal-layout {
    padding-bottom: 70px;
  }

  .mobile-tabs {
    display: flex;
  }

  .portal-footer {
    display: none;
  }
}
</style>
