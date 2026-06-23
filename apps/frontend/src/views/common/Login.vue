<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Lock, User } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { authApi } from '@/api/authApi';
import { defaultPathForRole, setAuthState } from '@/utils/auth';

const route = useRoute();
const router = useRouter();

const isRegister = ref(false);
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

    <div class="login-card">
      <div class="login-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="14" fill="url(#loginGrad)" />
            <path d="M14 34V20l10 8 10-8v14" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
            <defs>
              <linearGradient id="loginGrad" x1="0" y1="0" x2="48" y2="48">
                <stop stop-color="#0ea5e9" />
                <stop offset="1" stop-color="#2563eb" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h2>民宿预约管理系统</h2>
        <p>发现美好住宿体验</p>
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

.login-card {
  position: relative;
  width: 100%;
  max-width: 420px;
  padding: 40px 36px;
  background: rgb(255 255 255 / 95%);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.login-brand {
  text-align: center;
  margin-bottom: 32px;
}

.brand-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
}

.brand-icon svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 10px rgb(14 165 233 / 30%));
}

.login-brand h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.5px;
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

@media (max-width: 560px) {
  .login-card {
    padding: 28px 20px;
  }
}
</style>
