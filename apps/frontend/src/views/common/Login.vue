<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Lock, User } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { authApi } from '@/api/authApi';
import { defaultPathForRole, setAuthState } from '@/utils/auth';

const route = useRoute();
const router = useRouter();

const isRegister = ref(false);

onMounted(() => {
  if (route.query.mode === 'register') {
    isRegister.value = true;
  }
});
const loginForm = ref({ username: '', password: '' });
const registerForm = ref({ username: '', password: '', confirmPassword: '', name: '' });
const loading = ref(false);

async function handleLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
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
  } finally {
    loading.value = false;
  }
}

async function handleRegister() {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning('请填写用户名和密码');
    return;
  }
  if (registerForm.value.username.length < 3 || registerForm.value.username.length > 20) {
    ElMessage.warning('用户名必须为3-20位');
    return;
  }
  if (registerForm.value.password.length < 6 || registerForm.value.password.length > 20) {
    ElMessage.warning('密码必须为6-20位');
    return;
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次密码输入不一致');
    return;
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
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-orb orb-1" />
      <div class="bg-orb orb-2" />
    </div>

    <div class="login-wrapper">
      <!-- 左侧装饰面板 -->
      <div class="login-decor">
        <div class="decor-content">
          <div class="decor-icon">
            <svg viewBox="0 0 64 64" fill="none">
              <rect width="64" height="64" rx="18" fill="url(#decorGrad)" opacity="0.15" />
              <path d="M20 44V24l12 9 12-9v20" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
              <defs>
                <linearGradient id="decorGrad" x1="0" y1="0" x2="64" y2="64">
                  <stop stop-color="#0ea5e9" />
                  <stop offset="1" stop-color="#2563eb" />
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h2>欢迎来到<br />民宿预约平台</h2>
          <p>精选全国优质民宿，为您提供舒适的住宿体验。无论是周末度假还是长期旅居，都能找到理想的居所。</p>
          <div class="decor-features">
            <div class="decor-feature">
              <span class="feature-icon">🏠</span>
              <span>精选房源</span>
            </div>
            <div class="decor-feature">
              <span class="feature-icon">🔒</span>
              <span>安全支付</span>
            </div>
            <div class="decor-feature">
              <span class="feature-icon">⭐</span>
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
          <p>{{ isRegister ? '创建账号，开始预订民宿' : '登录您的账号' }}</p>
        </div>

      <!-- 登录表单 -->
      <template v-if="!isRegister">
        <el-form :model="loginForm" class="login-form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input
              v-model="loginForm.username"
              :prefix-icon="User"
              placeholder="请输入用户名"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="loginForm.password"
              :prefix-icon="Lock"
              type="password"
              show-password
              placeholder="请输入密码"
              size="large"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
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
            <el-input
              v-model="registerForm.username"
              :prefix-icon="User"
              placeholder="用户名（3-20位）"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="registerForm.name"
              :prefix-icon="User"
              placeholder="昵称（选填）"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="registerForm.password"
              :prefix-icon="Lock"
              type="password"
              show-password
              placeholder="密码（6-20位）"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="registerForm.confirmPassword"
              :prefix-icon="Lock"
              type="password"
              show-password
              placeholder="确认密码"
              size="large"
              @keyup.enter="handleRegister"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleRegister"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-switch">
          <span>已有账号？</span>
          <el-button type="primary" link @click="isRegister = false">返回登录</el-button>
        </div>
      </template>

      <!-- 演示账号提示 -->
      <div class="demo-hint">
        <span class="hint-label">演示账号：</span>
        <span>管理员 <strong>admin</strong> / <strong>admin123</strong></span>
      </div>
    </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
  overflow: hidden;
  background:
    radial-gradient(ellipse 80% 60% at 15% 10%, rgb(14 165 233 / 8%), transparent),
    radial-gradient(ellipse 60% 50% at 85% 90%, rgb(37 99 235 / 6%), transparent),
    var(--c-bg);
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.4;
}

.orb-1 {
  width: 500px;
  height: 500px;
  top: -200px;
  left: -100px;
  background: rgb(14 165 233 / 10%);
}

.orb-2 {
  width: 400px;
  height: 400px;
  bottom: -150px;
  right: -80px;
  background: rgb(37 99 235 / 8%);
}

.login-wrapper {
  position: relative;
  display: flex;
  max-width: 900px;
  width: 100%;
  min-height: 520px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

.login-decor {
  flex: 1;
  background: linear-gradient(135deg, #0ea5e9 0%, #2563eb 50%, #1d4ed8 100%);
  padding: 48px 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-decor::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgb(255 255 255 / 8%), transparent 50%),
    radial-gradient(circle at 80% 20%, rgb(255 255 255 / 5%), transparent 50%);
}

.decor-content {
  position: relative;
  z-index: 1;
  color: #fff;
}

.decor-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 24px;
}

.decor-icon svg {
  width: 100%;
  height: 100%;
}

.decor-content h2 {
  margin: 0 0 16px;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.3;
  letter-spacing: -0.5px;
}

.decor-content > p {
  margin: 0 0 32px;
  font-size: 15px;
  line-height: 1.7;
  color: rgb(255 255 255 / 80%);
}

.decor-features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.decor-feature {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: rgb(255 255 255 / 90%);
}

.feature-icon {
  font-size: 18px;
}

.login-card {
  position: relative;
  width: 380px;
  flex-shrink: 0;
  padding: 40px 36px;
  background: var(--c-surface);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.card-top-link {
  margin-bottom: 8px;
}

.card-top-link .el-button {
  font-size: 14px;
}

.login-brand {
  text-align: center;
  margin-bottom: 28px;
}

.login-brand h2 {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--c-ink);
}

.login-brand p {
  margin: 0;
  color: var(--c-muted);
  font-size: 14px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  padding: 4px 12px;
}

.login-form :deep(.el-input__inner) {
  height: 44px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  border-radius: var(--radius-md);
  letter-spacing: 2px;
}

.form-switch {
  text-align: center;
  color: var(--c-muted);
  font-size: 14px;
  margin-top: -4px;
}

.demo-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 24px;
  padding-top: 20px;
  color: var(--c-muted);
  font-size: 13px;
  border-top: 1px dashed var(--c-line);
}

.hint-label {
  font-weight: 500;
}

.demo-hint strong {
  color: var(--c-ink-light);
  font-weight: 600;
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    min-height: auto;
    max-width: 420px;
  }

  .login-decor {
    padding: 32px 28px;
  }

  .decor-content h2 {
    font-size: 22px;
  }

  .decor-features {
    flex-direction: row;
    gap: 16px;
  }

  .login-card {
    width: 100%;
    padding: 32px 28px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 0;
  }

  .login-wrapper {
    border-radius: 0;
    min-height: 100vh;
  }

  .login-decor {
    display: none;
  }

  .login-card {
    padding: 28px 20px;
  }
}
</style>
