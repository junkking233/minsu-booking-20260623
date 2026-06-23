<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { userApi } from '@/api/userApi';

interface UserRecord {
  id: number;
  username: string;
  name: string;
  phone: string;
  avatar: string;
  role: string;
  status: number;
  orderCount: number;
  createTime: string;
}

const users = ref<UserRecord[]>([]);
const total = ref(0);
const loading = ref(false);

const filters = reactive({ keyword: '', status: undefined as number | undefined });
const pagination = reactive({ page: 1, pageSize: 20 });

async function loadUsers() {
  loading.value = true;
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize };
    if (filters.keyword) params.keyword = filters.keyword;
    if (filters.status != null) params.status = filters.status;
    const data = await userApi.list(params) as { records: UserRecord[]; total: number };
    users.value = data.records || [];
    total.value = data.total || 0;
  } catch (e) {
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  pagination.page = 1;
  loadUsers();
}

function onReset() {
  filters.keyword = '';
  filters.status = undefined;
  onSearch();
}

async function toggleStatus(row: UserRecord) {
  const newStatus = row.status === 1 ? 0 : 1;
  const label = newStatus === 1 ? '启用' : '禁用';
  try {
    await ElMessageBox.confirm(`确定${label}用户「${row.username}」？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await userApi.toggleStatus(row.id, newStatus);
    ElMessage.success(`${label}成功`);
    loadUsers();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

onMounted(loadUsers);
</script>

<template>
  <div class="user-manage">
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-input v-model="filters.keyword" placeholder="搜索用户名/昵称/手机号" :prefix-icon="Search" clearable class="w-240" @keyup.enter="onSearch" @clear="onSearch" />
        <el-select v-model="filters.status" placeholder="状态" clearable class="w-120" @change="onSearch">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="onReset">重置</el-button>
      </div>
    </div>

    <el-table :data="users" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar :size="36" :src="row.avatar">
            {{ row.name?.charAt(0) || row.username?.charAt(0) }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="昵称" width="100" />
      <el-table-column prop="username" label="用户名" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="createTime" label="注册时间" width="170">
        <template #default="{ row }">{{ row.createTime?.replace('T', ' ') }}</template>
      </el-table-column>
      <el-table-column prop="orderCount" label="订单数" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadUsers"
      />
    </div>
  </div>
</template>

<style scoped>
.user-manage {
  max-width: 1400px;
}

.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.w-240 { width: 240px; }
.w-120 { width: 120px; }
</style>
