<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
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
            <div class="order-top">
              <img :src="order.houseCoverImage || '/placeholder.svg'" class="order-img" />
              <div class="order-info">
                <h4>{{ order.houseName }}</h4>
                <div class="order-meta">
                  <span>{{ order.houseCity }}</span>
                  <span class="meta-sep">·</span>
                  <span>{{ order.checkIn?.slice(5) }} ~ {{ order.checkOut?.slice(5) }}</span>
                  <span class="meta-sep">·</span>
                  <span>{{ order.nights }} 晚</span>
                  <span class="meta-sep">·</span>
                  <span>{{ order.guests }} 人</span>
                </div>
              </div>
              <div class="order-right">
                <el-tag size="small" :type="order.status === 'WAIT_PAY' ? 'danger' : order.status === 'CHECKED_IN' ? 'success' : order.status === 'REVIEWED' ? 'success' : order.status === 'CANCELLED' ? 'info' : ''">
                  {{ order.statusName }}
                </el-tag>
                <div class="order-amount text-price">&yen;{{ order.amount }}</div>
                <div class="order-no">{{ order.orderNo }}</div>
              </div>
            </div>

            <div class="order-actions" v-if="statusActions[order.status]" @click.stop>
              <el-button
                v-for="a in statusActions[order.status]"
                :key="a.action" :type="a.type as any"
                size="small"
                @click="doAction(order.orderNo, a.action)"
              >{{ a.label }}</el-button>
              <el-button size="small" @click="goDetail(order.orderNo)">详情</el-button>
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
.orders-page { max-width: 900px; margin: 0 auto; padding: 24px 20px; }

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
.order-list { display: flex; flex-direction: column; gap: 14px; }
.order-card {
  position: relative; display: flex;
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

.order-body { flex: 1; padding: 16px 18px; min-width: 0; }

.order-top { display: flex; gap: 16px; align-items: center; }
.order-img {
  width: 100px; height: 80px;
  object-fit: cover; border-radius: var(--radius-sm); flex-shrink: 0;
}
.order-info { flex: 1; min-width: 0; }
.order-info h4 {
  margin: 0 0 6px; font-size: 15px; font-weight: 600; color: var(--c-ink);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.order-meta { display: flex; flex-wrap: wrap; gap: 2px; font-size: 13px; color: var(--c-muted); }
.meta-sep { color: var(--c-muted-light); }

.order-right { text-align: right; flex-shrink: 0; }
.order-amount {
  font-size: 20px; font-weight: 700; margin-top: 6px;
}
.order-no { font-size: 12px; color: var(--c-muted-light); margin-top: 2px; }

.order-actions {
  display: flex; gap: 8px; margin-top: 14px; padding-top: 14px;
  border-top: 1px solid var(--c-line-light); justify-content: flex-end;
}

@media (max-width: 640px) {
  .order-top { flex-direction: column; align-items: flex-start; }
  .order-img { width: 100%; height: 140px; }
  .order-right {
    width: 100%; display: flex; justify-content: space-between; align-items: center;
  }
}
</style>
