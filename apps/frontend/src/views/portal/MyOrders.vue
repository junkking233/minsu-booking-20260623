<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Calendar, House, Location, User } from '@element-plus/icons-vue';
import { orderApi } from '@/api/orderApi';
import EmptyState from '@/components/common/EmptyState.vue';

const router = useRouter();

interface OrderItem {
  orderNo: string; houseId: number; houseName: string; houseCoverImage: string;
  houseCity: string; checkIn: string; checkOut: string; nights: number;
  guests: number; amount: number; status: string; statusName: string; createTime: string;
}

const activeTab = ref('');
const orders = ref<OrderItem[]>([]);
const total = ref(0);
const loading = ref(false);
const page = ref(1);
const pageSize = 20;

const tabs = [
  { label: '全部', value: '' },
  { label: '待支付', value: 'WAIT_PAY' },
  { label: '已预订', value: 'BOOKED' },
  { label: '入住中', value: 'CHECKED_IN' },
  { label: '已完成', value: 'COMPLETED' },
];

const statusClass: Record<string, string> = {
  PENDING: 's-pending', WAIT_PAY: 's-waitpay', BOOKED: 's-booked',
  CHECKED_IN: 's-checkedin', COMPLETED: 's-completed',
  REVIEWED: 's-reviewed', CANCELLED: 's-cancelled',
  REFUNDING: 's-refunding', REFUNDED: 's-refunded',
};

const statusActions: Record<string, { label: string; action: string; type: string }[]> = {
  PENDING: [{ label: '取消', action: 'cancel', type: 'danger' }],
  WAIT_PAY: [
    { label: '去支付', action: 'pay', type: 'primary' },
    { label: '取消', action: 'cancel', type: 'danger' },
  ],
  BOOKED: [{ label: '取消', action: 'cancel', type: 'danger' }],
  COMPLETED: [{ label: '评价', action: 'review', type: 'primary' }],
};

const statusNameMap: Record<string, string> = {
  PENDING: '待确认',
  WAIT_PAY: '待支付',
  BOOKED: '已预订',
  CHECKED_IN: '入住中',
  COMPLETED: '已完成',
  REVIEWED: '已评价',
  CANCELLED: '已取消',
  REFUNDING: '退款中',
  REFUNDED: '已退款',
};

function displayStatus(order: OrderItem) {
  return order.statusName || statusNameMap[order.status] || '订单';
}

function displayTitle(order: OrderItem) {
  return order.houseName || '房源信息待同步';
}

function displayCity(order: OrderItem) {
  return order.houseCity || '城市待补充';
}

function displayDate(date?: string) {
  return date ? date.slice(5, 10).replace('-', '.') : '--';
}

function displayAmount(amount: number) {
  return Number(amount || 0).toFixed(0);
}

function usePlaceholder(e: Event) {
  const img = e.target as HTMLImageElement;
  if (!img.src.endsWith('/placeholder.svg')) {
    img.src = '/placeholder.svg';
  }
}

async function loadOrders() {
  loading.value = true;
  try {
    const params: Record<string, any> = { page: page.value, pageSize };
    if (activeTab.value) params.status = activeTab.value;
    const data = await orderApi.myOrders(params) as { records: OrderItem[]; total: number };
    orders.value = data.records || [];
    total.value = data.total || 0;
  } catch { ElMessage.error('加载订单失败'); }
  finally { loading.value = false; }
}

function onTabChange() { page.value = 1; loadOrders(); }
function goDetail(orderNo: string) { router.push(`/portal/orders/${orderNo}`); }

async function handleCancel(orderNo: string) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await orderApi.cancel(orderNo);
    ElMessage.success('订单已取消'); loadOrders();
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '操作失败'); }
}
async function handlePay(orderNo: string) {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '支付确认', { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'info' });
    await orderApi.pay(orderNo);
    ElMessage.success('支付成功'); loadOrders();
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '支付失败'); }
}
function handleReview(orderNo: string) { router.push(`/portal/review/${orderNo}`); }
function doAction(orderNo: string, action: string) {
  switch (action) { case 'cancel': handleCancel(orderNo); break; case 'pay': handlePay(orderNo); break; case 'review': handleReview(orderNo); break; }
}
function onPageChange(p: number) { page.value = p; loadOrders(); }

onMounted(loadOrders);
</script>

<template>
  <div class="orders-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">我的订单</h1>
        <span class="order-count" v-if="total > 0">共 {{ total }} 笔</span>
      </div>
    </div>

    <!-- 标签切换 -->
    <div class="pill-tabs">
      <button
        v-for="t in tabs" :key="t.value"
        class="pill-tab"
        :class="{ active: activeTab === t.value }"
        @click="activeTab = t.value; onTabChange()"
      >
        {{ t.label }}
      </button>
    </div>

    <div v-loading="loading">
      <!-- 订单卡片 -->
      <div v-if="orders.length > 0" class="order-list">
        <div
          v-for="order in orders"
          :key="order.orderNo"
          class="order-card"
          :class="statusClass[order.status]"
          @click="goDetail(order.orderNo)"
        >
          <div class="order-status-bar"></div>
          <div class="order-body">
            <div class="order-main">
              <div class="cover-wrap">
                <img
                  :src="order.houseCoverImage || '/placeholder.svg'"
                  :alt="displayTitle(order)"
                  class="order-img"
                  loading="lazy"
                  @error="usePlaceholder"
                />
              </div>

              <div class="order-info">
                <div class="order-heading">
                  <div class="title-area">
                    <h4>{{ displayTitle(order) }}</h4>
                    <div class="order-no">订单号 {{ order.orderNo }}</div>
                  </div>
                  <el-tag size="small" effect="plain" :type="order.status === 'WAIT_PAY' ? 'danger' : order.status === 'CHECKED_IN' ? 'success' : order.status === 'REVIEWED' ? 'success' : order.status === 'CANCELLED' ? 'info' : ''">
                    {{ displayStatus(order) }}
                  </el-tag>
                </div>

                <div class="order-meta-grid">
                  <div class="meta-item">
                    <el-icon><Location /></el-icon>
                    <span>{{ displayCity(order) }}</span>
                  </div>
                  <div class="meta-item">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ displayDate(order.checkIn) }} - {{ displayDate(order.checkOut) }}</span>
                  </div>
                  <div class="meta-item">
                    <el-icon><User /></el-icon>
                    <span>{{ order.nights || 0 }} 晚 · {{ order.guests || 1 }} 人</span>
                  </div>
                  <div class="meta-item">
                    <el-icon><House /></el-icon>
                    <span>房源详情</span>
                  </div>
                </div>
              </div>

              <div class="order-right">
                <div class="amount-label">订单金额</div>
                <div class="order-amount text-price">&yen;{{ displayAmount(order.amount) }}</div>
                <div class="order-actions" @click.stop>
                  <template v-if="statusActions[order.status]">
                    <el-button
                      v-for="a in statusActions[order.status]"
                      :key="a.action" :type="a.type as any"
                      :plain="a.action === 'cancel'"
                      size="small"
                      @click="doAction(order.orderNo, a.action)"
                    >{{ a.label }}</el-button>
                  </template>
                  <el-button size="small" @click="goDetail(order.orderNo)">详情</el-button>
                </div>
              </div>
            </div>

            <div class="order-footer">
              <span>创建时间 {{ order.createTime || '--' }}</span>
              <div class="footer-link">
                <span>查看订单</span>
                <span class="arrow">›</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <EmptyState v-else-if="!loading" type="orders" :show-action="true" @action="router.push('/portal/houses')">
        <template #action-text>去预订民宿</template>
      </EmptyState>

      <div class="pagination-bar" v-if="total > 0">
        <el-pagination
          v-model:current-page="page" :page-size="pageSize" :total="total"
          layout="total, prev, pager, next" @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.orders-page { max-width: 960px; margin: 0 auto; padding: 24px 20px; }

.page-header { margin-bottom: 20px; display: flex; align-items: flex-end; justify-content: space-between; }
.page-title { margin: 0 0 4px; font-size: 26px; font-weight: 800; letter-spacing: -0.5px; color: var(--c-ink); }
.order-count { font-size: 14px; color: var(--c-muted); }

/* Pill Tabs */
.pill-tabs {
  display: flex; gap: 8px; margin-bottom: 24px; flex-wrap: wrap;
}
.pill-tab {
  padding: 8px 20px;
  font-size: 14px; font-weight: 500; color: var(--c-body);
  background: var(--c-surface);
  border: 1px solid var(--c-line); border-radius: var(--radius-full);
  cursor: pointer; transition: all var(--transition-base);
}
.pill-tab:hover { border-color: var(--c-primary-light); color: var(--c-primary); }
.pill-tab.active {
  background: var(--c-primary); color: #fff; border-color: var(--c-primary);
  box-shadow: 0 2px 8px rgb(2 132 199 / 30%);
}

/* Order Cards */
.order-list { display: flex; flex-direction: column; gap: 16px; }
.order-card {
  position: relative;
  display: flex;
  background: var(--c-surface); border: 1px solid rgb(229 232 236 / 70%);
  border-radius: var(--radius-lg); overflow: hidden;
  cursor: pointer; transition: all var(--transition-base);
  box-shadow: var(--shadow-sm);
}
.order-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }

.order-status-bar {
  width: 4px; flex-shrink: 0;
  background: var(--c-line);
}
.s-pending .order-status-bar { background: var(--c-amber); }
.s-waitpay .order-status-bar { background: var(--c-coral); }
.s-booked .order-status-bar { background: var(--c-primary); }
.s-checkedin .order-status-bar { background: var(--c-sage); }
.s-completed .order-status-bar,
.s-reviewed .order-status-bar { background: var(--c-green); }
.s-cancelled .order-status-bar,
.s-refunded .order-status-bar { background: var(--c-muted-light); }
.s-refunding .order-status-bar { background: var(--c-amber); }

.order-body { flex: 1; padding: 20px 22px 14px; min-width: 0; }

.order-main {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr) 148px;
  gap: 16px;
  align-items: stretch;
}
.cover-wrap {
  position: relative;
  width: 96px;
  height: 72px;
  overflow: hidden;
  border-radius: var(--radius-md);
  background: var(--c-line-light);
  border: 1px solid rgb(229 232 236 / 80%);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 60%);
}
.order-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform var(--transition-base);
}
.order-card:hover .order-img { transform: scale(1.04); }
.order-info {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: space-between;
  gap: 14px;
}
.order-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}
.title-area { min-width: 0; }
.order-info h4 {
  margin: 0 0 6px;
  font-size: 17px;
  font-weight: 800;
  color: var(--c-ink);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.order-meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding: 8px 9px;
  background: var(--c-line-light);
  border-radius: var(--radius-sm);
}
.meta-item .el-icon {
  flex: 0 0 auto;
  font-size: 14px;
  color: var(--c-muted);
}
.meta-item span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  font-weight: 600;
  color: var(--c-ink-light);
}

.order-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
  min-width: 0;
  padding-left: 18px;
  text-align: right;
  flex-shrink: 0;
  border-left: 1px solid var(--c-line-light);
}
.order-amount {
  font-size: 24px; font-weight: 800; line-height: 1.1;
  color: var(--c-price);
}
.amount-label {
  font-size: 12px;
  color: var(--c-muted);
}
.order-no {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: var(--c-muted-light);
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding-top: 12px;
  border-top: 1px solid var(--c-line-light);
  font-size: 12px;
  color: var(--c-muted-light);
}
.order-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  flex-wrap: wrap;
}
.order-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}
.footer-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--c-primary);
  font-weight: 600;
}
.arrow {
  font-size: 18px;
  line-height: 1;
}

@media (max-width: 640px) {
  .order-main { grid-template-columns: 1fr; align-items: flex-start; }
  .cover-wrap { width: 100%; height: 150px; }
  .order-meta-grid { grid-template-columns: 1fr 1fr; width: 100%; }
  .order-right {
    width: 100%;
    align-items: flex-start;
    padding-left: 0;
    border-left: 0;
    text-align: left;
  }
  .order-footer {
    align-items: stretch;
    flex-direction: column;
    gap: 10px;
  }
  .order-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
