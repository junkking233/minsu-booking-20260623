<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { orderApi } from '@/api/orderApi';

const route = useRoute();
const router = useRouter();
const orderNo = route.params.orderNo as string;

interface OrderDetail {
  orderNo: string;
  houseId: number;
  houseName: string;
  houseCoverImage: string;
  houseCity: string;
  houseAddress: string;
  userId: number;
  userName: string;
  guestName: string;
  phone: string;
  checkIn: string;
  checkOut: string;
  nights: number;
  guests: number;
  amount: number;
  status: string;
  statusName: string;
  paid: boolean;
  note: string;
  createTime: string;
}

const order = ref<OrderDetail | null>(null);
const loading = ref(true);

const statusFlow = [
  { status: 'PENDING', label: '待确认', icon: 'Clock' },
  { status: 'WAIT_PAY', label: '待支付', icon: 'Wallet' },
  { status: 'BOOKED', label: '已预订', icon: 'Check' },
  { status: 'CHECKED_IN', label: '入住中', icon: 'Key' },
  { status: 'COMPLETED', label: '已完成', icon: 'CircleCheck' },
];

const cancelStatuses = ['CANCELLED', 'REFUNDING', 'REFUNDED'];
const statusColors: Record<string, string> = {
  PENDING: 'warning',
  WAIT_PAY: 'danger',
  BOOKED: '',
  CHECKED_IN: 'success',
  COMPLETED: 'info',
  REVIEWED: 'success',
  CANCELLED: 'info',
  REFUNDING: 'warning',
  REFUNDED: 'info',
};

const currentStep = ref(-1);

function hasActions(status: string) {
  return status === 'PENDING' || status === 'WAIT_PAY' || status === 'BOOKED' || status === 'COMPLETED';
}

async function loadOrder() {
  loading.value = true;
  try {
    const data = await orderApi.getDetail(orderNo) as OrderDetail;
    order.value = data;
    const idx = statusFlow.findIndex((s) => s.status === data.status);
    currentStep.value = idx >= 0 ? idx + 1 : 0;
  } catch (e) {
    ElMessage.error('加载订单失败');
  } finally {
    loading.value = false;
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await orderApi.cancel(orderNo);
    ElMessage.success('订单已取消');
    loadOrder();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

async function handlePay() {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '支付确认', { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'info' });
    await orderApi.pay(orderNo);
    ElMessage.success('支付成功');
    loadOrder();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '支付失败');
  }
}

function handleReview() {
  router.push(`/portal/review/${orderNo}`);
}

onMounted(loadOrder);
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <div class="page-inner" v-if="order">
      <h1 class="page-title">订单详情</h1>

      <!-- 进度条 -->
      <el-card class="progress-card" v-if="!cancelStatuses.includes(order.status)">
        <el-steps :active="currentStep" align-center>
          <el-step
            v-for="s in statusFlow"
            :key="s.status"
            :title="s.label"
          />
        </el-steps>
        <div class="current-status" v-if="order.status === 'REVIEWED'">
          已评价
        </div>
      </el-card>

      <div class="detail-grid">
        <!-- 左侧信息 -->
        <div class="detail-main">
          <!-- 订单状态 -->
          <el-card class="info-card">
            <div class="status-header">
              <el-tag :type="statusColors[order.status] as any" size="large">{{ order.statusName }}</el-tag>
              <span class="order-no">{{ order.orderNo }}</span>
            </div>
            <p class="order-time">创建时间：{{ order.createTime?.replace('T', ' ') }}</p>
          </el-card>

          <!-- 房源信息 -->
          <el-card class="info-card">
            <h3>房源信息</h3>
            <div class="house-info">
              <img
                :src="order.houseCoverImage || '/placeholder.svg'"
                class="house-img"
              />
              <div class="house-text">
                <h4>{{ order.houseName }}</h4>
                <p>{{ order.houseCity }}</p>
                <p>{{ order.houseAddress }}</p>
              </div>
            </div>
          </el-card>

          <!-- 住客信息 -->
          <el-card class="info-card">
            <h3>住客信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <label>联系人</label>
                <span>{{ order.guestName }}</span>
              </div>
              <div class="info-item">
                <label>手机号</label>
                <span>{{ order.phone }}</span>
              </div>
              <div class="info-item">
                <label>入住日期</label>
                <span>{{ order.checkIn }}</span>
              </div>
              <div class="info-item">
                <label>离店日期</label>
                <span>{{ order.checkOut }}</span>
              </div>
              <div class="info-item">
                <label>入住晚数</label>
                <span>{{ order.nights }} 晚</span>
              </div>
              <div class="info-item">
                <label>入住人数</label>
                <span>{{ order.guests }} 人</span>
              </div>
            </div>
            <div class="info-item" v-if="order.note">
              <label>备注</label>
              <span>{{ order.note }}</span>
            </div>
          </el-card>
        </div>

        <!-- 右侧操作 -->
        <div class="detail-sidebar">
          <el-card class="amount-card">
            <div class="amount-label">订单金额</div>
            <div class="amount-value">&yen;{{ order.amount }}</div>
            <div class="amount-meta">
              <span class="paid-label">支付状态</span>
              <span class="paid-pill" :class="{ paid: order.paid }">
                {{ order.paid ? '已支付' : '未支付' }}
              </span>
            </div>
          </el-card>

          <el-card v-if="hasActions(order.status)" class="action-card">
            <div class="action-title">订单操作</div>
            <div class="action-list">
              <template v-if="order.status === 'WAIT_PAY'">
                <el-button type="primary" class="action-btn action-btn--primary" @click="handlePay">去支付</el-button>
              </template>
              <template v-if="order.status === 'COMPLETED'">
                <el-button type="success" class="action-btn" @click="handleReview">发表评价</el-button>
              </template>
              <template v-if="order.status === 'PENDING' || order.status === 'WAIT_PAY' || order.status === 'BOOKED'">
                <el-button plain type="danger" class="action-btn" @click="handleCancel">取消订单</el-button>
              </template>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  margin: 0 0 24px;
  font-size: 24px;
  font-weight: 800;
}

.progress-card {
  margin-bottom: 24px;
}

.current-status {
  text-align: center;
  margin-top: 12px;
  color: var(--c-primary);
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 22px;
  align-items: start;
}

.detail-main,
.detail-sidebar {
  min-width: 0;
}

.info-card {
  margin-bottom: 16px;
}

.info-card h3 {
  margin: 0 0 14px;
  font-size: 16px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.order-no {
  font-size: 14px;
  color: var(--c-muted);
}

.order-time {
  color: var(--c-muted);
  font-size: 13px;
  margin: 0;
}

.house-info {
  display: flex;
  gap: 14px;
}

.house-img {
  width: 160px;
  height: 120px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.house-text h4 {
  margin: 0 0 6px;
  font-size: 16px;
}

.house-text p {
  margin: 3px 0;
  font-size: 13px;
  color: var(--c-muted);
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item label {
  display: block;
  font-size: 12px;
  color: var(--c-muted);
  margin-bottom: 4px;
}

.info-item span {
  font-size: 14px;
  font-weight: 500;
}

.amount-card {
  text-align: center;
  margin-bottom: 16px;
}

.amount-card :deep(.el-card__body) {
  padding: 24px 20px 22px;
}

.amount-label {
  font-size: 13px;
  color: var(--c-muted);
  font-weight: 600;
}

.amount-value {
  font-size: 34px;
  font-weight: 800;
  color: var(--c-price);
  line-height: 1.15;
  margin: 10px 0 14px;
  letter-spacing: 0;
}

.amount-meta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: var(--radius-full);
  background: var(--c-line-light);
  font-size: 13px;
  color: var(--c-muted);
}

.paid-label {
  font-weight: 500;
}

.paid-pill {
  display: inline-flex;
  align-items: center;
  min-width: 52px;
  justify-content: center;
  padding: 2px 9px;
  border-radius: var(--radius-full);
  background: var(--c-surface);
  color: var(--c-muted);
  font-size: 12px;
  font-weight: 700;
}

.paid-pill.paid {
  background: var(--c-green-bg);
  color: var(--c-green);
}

.action-card {
  overflow: hidden;
}

.action-card :deep(.el-card__body) {
  padding: 18px;
}

.action-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 700;
  color: var(--c-ink);
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 700;
  border-radius: var(--radius-md);
  margin: 0;
}

.action-list :deep(.el-button + .el-button) {
  margin-left: 0;
}

.action-btn--primary {
  box-shadow: 0 8px 18px rgb(2 132 199 / 22%);
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .house-info {
    flex-direction: column;
  }

  .house-img {
    width: 100%;
    height: 180px;
  }
}
</style>
