<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { reviewApi } from '@/api/reviewApi';

interface ReviewRecord {
  id: number;
  userName: string;
  houseName: string;
  rating: number;
  cleanScore: number;
  locationScore: number;
  serviceScore: number;
  valueScore: number;
  content: string;
  images: string[];
  reply: string;
  status: string;
  createTime: string;
}

const reviews = ref<ReviewRecord[]>([]);
const total = ref(0);
const loading = ref(false);

const filters = reactive({ status: '' });
const pagination = reactive({ page: 1, pageSize: 20 });

const replyVisible = ref(false);
const replyContent = ref('');
const replyReviewId = ref(0);

async function loadReviews() {
  loading.value = true;
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize };
    if (filters.status) params.status = filters.status;
    const data = await reviewApi.adminList(params) as { records: ReviewRecord[]; total: number };
    reviews.value = data.records || [];
    total.value = data.total || 0;
  } catch (e) {
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  pagination.page = 1;
  loadReviews();
}

function openReply(row: ReviewRecord) {
  replyReviewId.value = row.id;
  replyContent.value = row.reply || '';
  replyVisible.value = true;
}

async function submitReply() {
  if (!replyContent.value) { ElMessage.warning('请输入回复内容'); return; }
  try {
    await reviewApi.adminReply(replyReviewId.value, replyContent.value);
    ElMessage.success('回复成功');
    replyVisible.value = false;
    loadReviews();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '回复失败');
  }
}

async function toggleStatus(row: ReviewRecord) {
  const newStatus = row.status === 'VISIBLE' ? 'HIDDEN' : 'VISIBLE';
  const label = newStatus === 'VISIBLE' ? '显示' : '隐藏';
  try {
    await ElMessageBox.confirm(`确定${label}该评价？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await reviewApi.adminToggleStatus(row.id, newStatus);
    ElMessage.success(`已${label}`);
    loadReviews();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

async function handleDelete(row: ReviewRecord) {
  try {
    await ElMessageBox.confirm('确定删除该评价？', '删除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'error' });
    await reviewApi.adminDelete(row.id);
    ElMessage.success('已删除');
    loadReviews();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '删除失败');
  }
}

onMounted(loadReviews);
</script>

<template>
  <div class="review-manage">
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-select v-model="filters.status" placeholder="状态" clearable class="w-140" @change="onSearch">
          <el-option label="显示" value="VISIBLE" />
          <el-option label="隐藏" value="HIDDEN" />
        </el-select>
        <el-button type="primary" @click="onSearch">筛选</el-button>
      </div>
    </div>

    <el-table :data="reviews" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="userName" label="用户" width="90" />
      <el-table-column prop="houseName" label="房源" min-width="120" />
      <el-table-column label="评分" width="180">
        <template #default="{ row }">
          <span>整体{{ row.rating }} 卫生{{ row.cleanScore }} 位置{{ row.locationScore }} 服务{{ row.serviceScore }} 性价比{{ row.valueScore }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'VISIBLE' ? 'success' : 'info'" size="small">{{ row.status === 'VISIBLE' ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openReply(row)">回复</el-button>
          <el-button size="small" :type="row.status === 'VISIBLE' ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 'VISIBLE' ? '隐藏' : '显示' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadReviews"
      />
    </div>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyVisible" title="回复评价" width="500px" destroy-on-close>
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="请输入回复内容..." />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.review-manage {
  max-width: 1400px;
}

.toolbar-left {
  display: flex;
  gap: 10px;
}

.w-140 { width: 140px; }
</style>
