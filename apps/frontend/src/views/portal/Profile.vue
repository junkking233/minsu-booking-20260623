<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User } from '@element-plus/icons-vue';
import { authApi } from '@/api/authApi';
import { orderApi } from '@/api/orderApi';
import { favoriteApi } from '@/api/favoriteApi';
import { clearAuthState, getCurrentUser, setAuthState, getAuthState } from '@/utils/auth';

const router = useRouter();
const currentUser = getCurrentUser();

const editForm = ref({ name: '', phone: '' });
const editing = ref(false);
const saving = ref(false);

const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' });
const showPasswordForm = ref(false);
const changingPassword = ref(false);

const orderStats = ref({ total: 0, pending: 0, completed: 0 });
const favCount = ref(0);

function startEdit() {
  editForm.value.name = currentUser?.name || '';
  editForm.value.phone = currentUser?.phone || '';
  editing.value = true;
}

function cancelEdit() {
  editing.value = false;
}

async function saveProfile() {
  saving.value = true;
  try {
    await authApi.updateProfile({
      name: editForm.value.name,
      phone: editForm.value.phone,
    });
    const state = getAuthState();
    if (state) {
      setAuthState(state.token, { ...state.user, name: editForm.value.name, phone: editForm.value.phone }, state.expiresAt);
    }
    ElMessage.success('保存成功');
    editing.value = false;
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function changePassword() {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写密码');
    return;
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次密码不一致');
    return;
  }
  changingPassword.value = true;
  try {
    await authApi.changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    });
    ElMessage.success('密码修改成功，请重新登录');
    clearAuthState();
    router.push('/login');
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '修改失败');
  } finally {
    changingPassword.value = false;
  }
}

function logout() {
  clearAuthState();
  router.push('/login');
}

async function loadStats() {
  try {
    const [ordersData, favsData] = await Promise.all([
      orderApi.myOrders({ pageSize: 1 }),
      favoriteApi.myList(),
    ]);
    const orders = ordersData as { total: number };
    const favs = favsData as any[];
    orderStats.value.total = orders.total || 0;
    favCount.value = favs.length || 0;

    // Load status counts
    const pending = await orderApi.myOrders({ status: 'PENDING', pageSize: 1 }) as { total: number };
    const completed = await orderApi.myOrders({ status: 'COMPLETED', pageSize: 1 }) as { total: number };
    orderStats.value.pending = pending.total || 0;
    orderStats.value.completed = completed.total || 0;
  } catch { /* stats are optional */ }
}

onMounted(() => {
  editForm.value.name = currentUser?.name || '';
  editForm.value.phone = currentUser?.phone || '';
  loadStats();
});
</script>

<template>
  <div class="profile-page">
    <div class="page-inner">
      <h1 class="page-title">个人中心</h1>

      <div class="profile-grid">
        <!-- 左侧个人信息 -->
        <div class="profile-main">
          <el-card class="profile-card">
            <div class="profile-header">
              <el-avatar :size="64" class="profile-avatar">
                <el-icon :size="36"><User /></el-icon>
              </el-avatar>
              <div class="profile-meta">
                <h2>{{ currentUser?.name || currentUser?.username }}</h2>
                <p>@{{ currentUser?.username }}</p>
              </div>
            </div>

            <!-- 编辑模式 -->
            <template v-if="editing">
              <el-form :model="editForm" label-width="60px" class="edit-form">
                <el-form-item label="昵称">
                  <el-input v-model="editForm.name" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="editForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
                  <el-button @click="cancelEdit">取消</el-button>
                </el-form-item>
              </el-form>
            </template>

            <!-- 查看模式 -->
            <template v-else>
              <div class="profile-info">
                <div class="info-row">
                  <label>昵称</label>
                  <span>{{ currentUser?.name || '-' }}</span>
                </div>
                <div class="info-row">
                  <label>手机号</label>
                  <span>{{ currentUser?.phone || '未绑定' }}</span>
                </div>
              </div>
              <el-button type="primary" @click="startEdit" class="edit-btn">编辑资料</el-button>
            </template>
          </el-card>

          <!-- 修改密码 -->
          <el-card class="password-card" v-if="showPasswordForm">
            <h3>修改密码</h3>
            <el-form :model="passwordForm" label-width="80px">
              <el-form-item label="原密码">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="passwordForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="changingPassword" @click="changePassword">确认修改</el-button>
                <el-button @click="showPasswordForm = false">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-button v-else type="warning" @click="showPasswordForm = true">修改密码</el-button>
        </div>

        <!-- 右侧统计 -->
        <div class="profile-sidebar">
          <el-card class="stats-card">
            <h3>订单统计</h3>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ orderStats.total }}</div>
                <div class="stat-label">全部订单</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ orderStats.pending }}</div>
                <div class="stat-label">待处理</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ orderStats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ favCount }}</div>
                <div class="stat-label">收藏房源</div>
              </div>
            </div>
          </el-card>

          <el-card class="logout-card">
            <el-button type="danger" class="logout-btn" @click="logout">退出登录</el-button>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  margin: 0 0 24px;
  font-size: 24px;
  font-weight: 800;
}

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 20px;
  align-items: start;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--c-line);
}

.profile-avatar {
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
  box-shadow: 0 4px 12px rgb(14 165 233 / 30%);
}

.profile-meta h2 {
  margin: 0 0 4px;
  font-size: 20px;
}

.profile-meta p {
  margin: 0;
  color: var(--c-muted);
  font-size: 14px;
}

.profile-info {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  padding: 8px 0;
  font-size: 14px;
}

.info-row label {
  width: 60px;
  color: var(--c-muted);
  flex-shrink: 0;
}

.edit-form {
  margin-top: 8px;
}

.edit-btn {
  border-radius: var(--radius-md);
}

.password-card {
  margin-top: 16px;
}

.password-card h3 {
  margin: 0 0 16px;
}

.stats-card {
  margin-bottom: 16px;
}

.stats-card h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px 12px;
  background: var(--c-line-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.stat-item:hover {
  background: var(--c-primary-bg);
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--c-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--c-muted);
  margin-top: 4px;
}

.logout-btn {
  width: 100%;
  border-radius: var(--radius-md);
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
