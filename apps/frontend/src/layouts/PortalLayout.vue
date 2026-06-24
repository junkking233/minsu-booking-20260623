<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ArrowDown, User, HomeFilled, House, Tickets, StarFilled
} from '@element-plus/icons-vue';
import { clearAuthState, getCurrentUser } from '@/utils/auth';

const route = useRoute();
const router = useRouter();
const currentUser = getCurrentUser();

const isAdmin = computed(() => currentUser?.role === 'ADMIN');

const navItems = [
  { label: '首页', path: '/portal/home', icon: HomeFilled },
  { label: '房源列表', path: '/portal/houses', icon: House },
  { label: '我的订单', path: '/portal/orders', icon: Tickets },
  { label: '我的收藏', path: '/portal/favorites', icon: StarFilled },
];

const activeMenu = computed(() => {
  if (route.path.startsWith('/portal/home')) return '/portal/home';
  if (route.path.startsWith('/portal/houses')) return '/portal/houses';
  if (route.path.startsWith('/portal/orders')) return '/portal/orders';
  if (route.path.startsWith('/portal/favorites')) return '/portal/favorites';
  return '/portal/home';
});

function handleDropdown(cmd: string) {
  if (cmd === 'profile') router.push('/portal/profile');
  else if (cmd === 'admin') router.push('/admin/dashboard');
  else if (cmd === 'logout') logout();
}

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
              <rect x="18" y="20" width="4" height="8" rx="1" fill="var(--c-coral)" opacity="0.9" />
              <defs>
                <linearGradient id="portalGrad" x1="0" y1="0" x2="40" y2="40">
                  <stop stop-color="#0ea5e9" />
                  <stop offset="1" stop-color="#0284c7" />
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
            <el-dropdown trigger="click" @command="handleDropdown">
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
                  <el-dropdown-item v-if="isAdmin" command="admin" divided>
                    <span class="admin-menu-item">管理后台</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" :divided="!isAdmin">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button class="register-btn" @click="router.push('/login?mode=register')">注册</el-button>
            <el-button type="primary" class="login-btn" @click="router.push('/login')">登录</el-button>
          </template>
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
        <div class="footer-top">
          <div class="footer-brand">
            <div class="footer-logo">
              <svg viewBox="0 0 32 32" fill="none">
                <rect width="32" height="32" rx="8" fill="url(#footerGrad)" />
                <path d="M10 22V12l6 5 6-5v10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                <defs>
                  <linearGradient id="footerGrad" x1="0" y1="0" x2="32" y2="32">
                    <stop stop-color="#0ea5e9" />
                    <stop offset="1" stop-color="#0284c7" />
                  </linearGradient>
                </defs>
              </svg>
              <span>民宿预约</span>
            </div>
            <p class="footer-desc">发现美好住宿体验，精选品质民宿，让每一次旅行都充满期待。</p>
          </div>
          <div class="footer-links">
            <div class="footer-col">
              <h4>快速导航</h4>
              <a @click.prevent="router.push('/portal/home')">首页</a>
              <a @click.prevent="router.push('/portal/houses')">房源列表</a>
              <a @click.prevent="router.push('/portal/orders')">我的订单</a>
            </div>
            <div class="footer-col">
              <h4>帮助支持</h4>
              <a href="#">使用指南</a>
              <a href="#">常见问题</a>
              <a href="#">联系客服</a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <span>&copy; 2026 民宿预约管理系统 All Rights Reserved</span>
        </div>
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
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </div>
      <div
        class="mobile-tab"
        :class="{ active: route.path === '/portal/profile' }"
        @click="currentUser ? router.push('/portal/profile') : router.push('/login')"
      >
        <el-icon><User /></el-icon>
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
    radial-gradient(ellipse 80% 60% at 10% 0%, rgb(2 132 199 / 4%), transparent),
    radial-gradient(ellipse 60% 50% at 90% 100%, rgb(234 115 86 / 3%), transparent),
    var(--c-bg);
  padding-bottom: 70px;
}

/* Header */
.portal-header {
  position: sticky;
  z-index: 50;
  top: 0;
  background: rgb(255 255 255 / 88%);
  border-bottom: 1px solid var(--c-line);
  backdrop-filter: blur(20px) saturate(1.8);
  -webkit-backdrop-filter: blur(20px) saturate(1.8);
  box-shadow: 0 1px 3px rgb(26 35 50 / 4%);
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
.brand:hover { opacity: 0.85; }

.brand-mark { width: 36px; height: 36px; }
.brand-mark svg {
  width: 100%; height: 100%;
  filter: drop-shadow(0 2px 4px rgb(2 132 199 / 30%));
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
.user-badge:hover { background: var(--c-line-light); }

.user-avatar {
  background: linear-gradient(135deg, #0ea5e9, var(--c-primary-dark));
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

.user-arrow { font-size: 12px; color: var(--c-muted); }

.admin-menu-item { color: var(--c-primary); font-weight: 600; }

.register-btn { font-weight: 600; border-radius: var(--radius-md); padding: 0 18px; }
.login-btn { font-weight: 600; border-radius: var(--radius-md); padding: 0 20px; }

/* Main */
.portal-main { flex: 1; min-width: 0; }

/* Footer */
.portal-footer {
  padding: 0;
  background: linear-gradient(180deg, #0f1a2b 0%, #0f172a 100%);
  color: #94a3b8;
}
.footer-inner { max-width: 1280px; margin: 0 auto; }

.footer-top {
  display: flex;
  justify-content: space-between;
  gap: 40px;
  padding: 40px 28px 32px;
}
.footer-brand { max-width: 360px; }

.footer-logo {
  display: flex; align-items: center; gap: 10px; margin-bottom: 12px;
}
.footer-logo svg { width: 28px; height: 28px; }
.footer-logo span { font-size: 18px; font-weight: 700; color: #f1f5f9; }

.footer-desc {
  font-size: 14px; line-height: 1.7; color: #64748b; margin: 0;
}

.footer-links { display: flex; gap: 60px; }

.footer-col h4 {
  margin: 0 0 16px;
  font-size: 14px; font-weight: 600; color: #e2e8f0;
}
.footer-col a {
  display: block; margin-bottom: 10px;
  font-size: 14px; color: #64748b;
  text-decoration: none; cursor: pointer;
  transition: color var(--transition-fast);
}
.footer-col a:hover { color: var(--c-coral-light); }

.footer-bottom {
  padding: 18px 28px;
  border-top: 1px solid rgb(255 255 255 / 6%);
  text-align: center; font-size: 13px; color: #475569;
}

/* Page transitions */
.page-enter-active,
.page-leave-active {
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}
.page-enter-from { opacity: 0; transform: translateY(10px); }
.page-leave-to   { opacity: 0; transform: translateY(-6px); }

/* Mobile tabs */
.mobile-tabs {
  display: none;
  position: fixed;
  bottom: 0; left: 0; right: 0; z-index: 40;
  height: 56px;
  padding-bottom: env(safe-area-inset-bottom, 0);
  background: rgb(255 255 255 / 95%);
  backdrop-filter: blur(16px) saturate(1.8);
  -webkit-backdrop-filter: blur(16px) saturate(1.8);
  border-top: 1px solid rgb(229 232 236 / 80%);
  box-shadow: 0 -2px 12px rgb(26 35 50 / 6%);
}

.mobile-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
  padding: 4px 0;
  font-size: 10px;
  color: var(--c-muted);
  cursor: pointer;
  transition: color var(--transition-fast);
  height: 56px;
}
.mobile-tab .el-icon { font-size: 20px; margin-bottom: 1px; }
.mobile-tab.active {
  color: var(--c-primary);
  font-weight: 600;
}

@media (max-width: 768px) {
  .header-inner { padding: 10px 16px; min-height: 52px; }
  .brand-text { font-size: 17px; }
  .portal-menu { display: none; }
  .portal-layout { padding-bottom: calc(56px + env(safe-area-inset-bottom, 0px)); }
  .mobile-tabs { display: flex; }
  .portal-footer { display: none; }
  .register-btn { display: none; }
}
</style>
