<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Lock, User, House, CreditCard, Service } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { authApi } from '@/api/authApi';
import { defaultPathForRole, setAuthState } from '@/utils/auth';

const route = useRoute();
const router = useRouter();

const isRegister = ref(false);

onMounted(() => {
  if (route.query.mode === 'register') isRegister.value = true;
});
const loginForm = ref({ username: '', password: '' });
const registerForm = ref({ username: '', password: '', confirmPassword: '', name: '' });
const loading = ref(false);

async function handleLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码'); return;
  }
  loading.value = true;
  try {
    const result = await authApi.login({
      username: loginForm.value.username,
      password: loginForm.value.password,
    });
    setAuthState(result.token, result.user, result.expiresAt);
    ElMessage.success('登录成功');
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : defaultPathForRole(result.user.role);
    await router.push(redirect);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败');
  } finally { loading.value = false; }
}

async function handleRegister() {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning('请填写用户名和密码'); return;
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次密码输入不一致'); return;
  }
  loading.value = true;
  try {
    await authApi.register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      name: registerForm.value.name || registerForm.value.username,
    });
    ElMessage.success('注册成功，请登录');
    isRegister.value = false;
    loginForm.value.username = registerForm.value.username;
    registerForm.value = { username: '', password: '', confirmPassword: '', name: '' };
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '注册失败');
  } finally { loading.value = false; }
}
</script>

<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-orb bg-orb--1"></div>
    <div class="bg-orb bg-orb--2"></div>
    <div class="bg-orb bg-orb--3"></div>

    <div class="login-wrapper">
      <!-- 左侧装饰面板 -->
      <div class="login-decor">
        <div class="decor-content">
          <div class="decor-logo">
            <svg viewBox="0 0 40 40" fill="none">
              <rect width="40" height="40" rx="10" fill="rgb(255 255 255 / 15%)" stroke="rgb(255 255 255 / 20%)" stroke-width="1.5"/>
              <path d="M12 28V14l8 7 8-7v14" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <rect x="18" y="20" width="4" height="8" rx="1" fill="var(--c-coral)" opacity="0.9"/>
            </svg>
          </div>
          <h2>欢迎来到<br />民宿预约平台</h2>
          <p>精选全国优质民宿，为您提供舒适的住宿体验。无论是周末度假还是长期旅居，都能找到理想温暖的居所。</p>
          <div class="decor-features">
            <div class="decor-feature">
              <span class="feat-icon-box"><el-icon><House /></el-icon></span>
              <span>精选房源</span>
            </div>
            <div class="decor-feature">
              <span class="feat-icon-box"><el-icon><CreditCard /></el-icon></span>
              <span>安全支付</span>
            </div>
            <div class="decor-feature">
              <span class="feat-icon-box"><el-icon><Service /></el-icon></span>
              <span>优质服务</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-card">
        <div class="card-top-link">
          <el-button link type="primary" @click="router.push('/portal/home')">&larr; 返回首页</el-button>
        </div>
        <div class="login-brand">
          <h2>{{ isRegister ? '注册新账号' : '欢迎回来' }}</h2>
          <p>{{ isRegister ? '创建账号，开始预订民宿' : '登录您的账号，继续旅程' }}</p>
        </div>

        <!-- 登录表单 -->
        <template v-if="!isRegister">
          <el-form :model="loginForm" class="login-form" @submit.prevent="handleLogin">
            <el-form-item>
              <el-input v-model="loginForm.username" :prefix-icon="User" placeholder="请输入用户名" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" :prefix-icon="Lock" type="password" show-password placeholder="请输入密码" size="large" @keyup.enter="handleLogin" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button>
            </el-form-item>
          </el-form>
          <div class="form-switch">
            <span>还没有账号？</span>
            <el-button type="primary" link @click="isRegister = true">立即注册</el-button>
          </div>
        </template>

        <!-- 注册表单 -->
        <template v-else>
          <el-form :model="registerForm" class="login-form" @submit.prevent="handleRegister">
            <el-form-item>
              <el-input v-model="registerForm.username" :prefix-icon="User" placeholder="用户名（3-20位）" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.name" :prefix-icon="User" placeholder="昵称（选填）" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.password" :prefix-icon="Lock" type="password" show-password placeholder="密码（6-20位）" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.confirmPassword" :prefix-icon="Lock" type="password" show-password placeholder="确认密码" size="large" @keyup.enter="handleRegister" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">注 册</el-button>
            </el-form-item>
          </el-form>
          <div class="form-switch">
            <span>已有账号？</span>
            <el-button type="primary" link @click="isRegister = false">返回登录</el-button>
          </div>
        </template>

        <!-- 分割线 -->
        <div class="form-divider"><span>演示账号</span></div>

        <div class="demo-hint">
          <div class="hint-row"><span>管理员</span><code>admin</code>/<code>admin123</code></div>
          <div class="hint-row"><span>普通用户</span><code>user</code>/<code>123456</code></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  display: flex; align-items: center; justify-content: center;
  min-height: 100vh; padding: 20px; overflow: hidden;
  background: linear-gradient(135deg, #f0f6fa 0%, #e8f0fe 50%, #faf7f2 100%);
}

/* 背景装饰球 */
.bg-orb {
  position: absolute; border-radius: 50%; filter: blur(100px); pointer-events: none;
}
.bg-orb--1 {
  width: 500px; height: 500px;
  top: -200px; right: -200px;
  background: rgb(2 132 199 / 6%);
}
.bg-orb--2 {
  width: 400px; height: 400px;
  bottom: -150px; left: -150px;
  background: rgb(234 115 86 / 6%);
}
.bg-orb--3 {
  width: 300px; height: 300px;
  top: 50%; left: 60%;
  background: rgb(2 132 199 / 4%);
}

/* Wrapper */
.login-wrapper {
  position: relative;
  display: flex;
  max-width: 900px; width: 100%;
  min-height: 540px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  background: #fff;
}

/* ---- 左侧装饰面板 ---- */
.login-decor {
  flex: 0 0 380px;
  background: linear-gradient(160deg, var(--c-primary) 0%, var(--c-primary-dark) 100%);
  padding: 48px 36px;
  display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden;
}
.login-decor::before {
  content: '';
  position: absolute; inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgb(255 255 255 / 10%), transparent 50%),
    radial-gradient(circle at 80% 20%, rgb(255 255 255 / 6%), transparent 50%);
}

.decor-content { position: relative; z-index: 1; color: #fff; }

.decor-logo {
  width: 56px; height: 56px;
  margin-bottom: 28px;
}
.decor-logo svg { width: 100%; height: 100%; filter: drop-shadow(0 4px 12px rgb(0 0 0 / 15%)); }

.decor-content h2 {
  margin: 0 0 16px;
  font-size: 26px; font-weight: 800; line-height: 1.3;
  letter-spacing: -0.3px;
}
.decor-content > p {
  margin: 0 0 32px;
  font-size: 14px; line-height: 1.8; color: rgb(255 255 255 / 80%);
}

.decor-features { display: flex; flex-direction: column; gap: 12px; }
.decor-feature {
  display: flex; align-items: center; gap: 12px;
  font-size: 14px; color: rgb(255 255 255 / 90%);
}
.feat-icon-box {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  background: rgb(255 255 255 / 12%);
  border-radius: var(--radius-sm);
  border: 1px solid rgb(255 255 255 / 15%);
  font-size: 16px;
}

/* ---- 右侧卡片 ---- */
.login-card {
  position: relative;
  flex: 1; padding: 40px 40px;
  background: var(--c-surface);
  display: flex; flex-direction: column; justify-content: center;
}
.card-top-link { margin-bottom: 8px; }

.login-brand { text-align: center; margin-bottom: 28px; }
.login-brand h2 {
  margin: 0 0 6px;
  font-size: 24px; font-weight: 800; letter-spacing: -0.5px; color: var(--c-ink);
}
.login-brand p {
  margin: 0; color: var(--c-muted); font-size: 14px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md); padding: 4px 12px;
}
.login-form :deep(.el-input__inner) { height: 44px; }

.submit-btn {
  width: 100%; height: 48px;
  font-size: 16px; font-weight: 700;
  border-radius: var(--radius-full); letter-spacing: 2px;
}

.form-switch {
  text-align: center; color: var(--c-muted);
  font-size: 14px; margin-top: -4px;
}

.form-divider {
  display: flex; align-items: center; gap: 12px;
  margin: 22px 0 14px; font-size: 12px; color: var(--c-muted-light);
}
.form-divider::before,
.form-divider::after {
  content: ''; flex: 1; height: 1px;
  background: var(--c-line);
}

.demo-hint { text-align: center; }
.hint-row {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 4px 0; font-size: 13px; color: var(--c-muted);
}
.hint-row span {
  display: inline-block; width: 56px;
  font-weight: 500; text-align: right;
  color: var(--c-muted-light);
}
.hint-row code {
  padding: 1px 8px;
  background: var(--c-bg);
  border-radius: var(--radius-sm);
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 12px; color: var(--c-ink-light);
  border: 1px solid var(--c-line);
}

/* Responsive */
@media (max-width: 768px) {
  .login-wrapper { flex-direction: column; min-height: auto; max-width: 420px; }
  .login-decor { flex: 0 0 auto; padding: 32px 28px; }
  .decor-content h2 { font-size: 22px; }
  .decor-features { flex-direction: row; gap: 16px; }
  .login-card { width: 100%; padding: 32px 28px; }
}
@media (max-width: 480px) {
  .login-page { padding: 0; }
  .login-wrapper { border-radius: 0; min-height: 100vh; }
  .login-decor { display: none; }
  .login-card { padding: 28px 20px; }
}
</style>
