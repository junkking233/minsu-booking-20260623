<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { orderApi } from '@/api/orderApi';

const router = useRouter();

interface OrderItem {
  orderNo: string;
  houseId: number;
  houseName: string;
  houseCoverImage: string;
  houseCity: string;
  checkIn: string;
  checkOut: string;
  nights: number;
  guests: number;
  amount: number;
  status: string;
  statusName: string;
  createTime: string;
}

const activeTab = ref('');
const orders = ref<OrderItem[]>([]);
const total = ref(0);
const loading = ref(false);
const page = ref(1);
const pageSize = 20;

const tabs = [
  { label: '全部', value: '' },
  { label: '待确认', value: 'PENDING' },
  { label: '待支付', value: 'WAIT_PAY' },
  { label: '已预订', value: 'BOOKED' },
  { label: '入住中', value: 'CHECKED_IN' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已评价', value: 'REVIEWED' },
  { label: '已取消', value: 'CANCELLED' },
];

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

const statusActions: Record<string, { label: string; action: string }[]> = {
  PENDING: [{ label: '取消订单', action: 'cancel' }],
  WAIT_PAY: [
    { label: '去支付', action: 'pay' },
    { label: '取消订单', action: 'cancel' },
  ],
  BOOKED: [{ label: '取消订单', action: 'cancel' }],
  COMPLETED: [{ label: '发表评价', action: 'review' }],
};

async function loadOrders() {
  loading.value = true;
  try {
    const params: Record<string, any> = { page: page.value, pageSize };
    if (activeTab.value) params.status = activeTab.value;
    const data = await orderApi.myOrders(params) as { records: OrderItem[]; total: number };
    orders.value = data.records || [];
    total.value = data.total || 0;
  } catch (e) {
    ElMessage.error('加载订单失败');
  } finally {
    loading.value = false;
  }
}

function onTabChange() {
  page.value = 1;
  loadOrders();
}

function goDetail(orderNo: string) {
  router.push(`/portal/orders/${orderNo}`);
}

async function handleCancel(orderNo: string) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await orderApi.cancel(orderNo);
    ElMessage.success('订单已取消');
    loadOrders();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

async function handlePay(orderNo: string) {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '支付确认', { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'info' });
    await orderApi.pay(orderNo);
    ElMessage.success('支付成功');
    loadOrders();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '支付失败');
  }
}

function handleReview(orderNo: string) {
  router.push(`/portal/review/${orderNo}`);
}

function doAction(orderNo: string, action: string) {
  switch (action) {
    case 'cancel': handleCancel(orderNo); break;
    case 'pay': handlePay(orderNo); break;
    case 'review': handleReview(orderNo); break;
  }
}

function onPageChange(p: number) {
  page.value = p;
  loadOrders();
}

onMounted(loadOrders);
</script>

<template>
  <div class="orders-page">
    <div class="page-inner">
      <h1 class="page-title">我的订单</h1>

      <el-tabs v-model="activeTab" @tab-change="onTabChange" class="order-tabs">
        <el-tab-pane v-for="t in tabs" :key="t.value" :label="t.label" :name="t.value" />
      </el-tabs>

      <div v-loading="loading">
        <div v-if="orders.length > 0" class="order-list">
          <el-card
            v-for="order in orders"
            :key="order.orderNo"
            class="order-card"
            shadow="hover"
            @click="goDetail(order.orderNo)"
          >
            <div class="order-top">
              <img
                :src="order.houseCoverImage || '/placeholder.svg'"
                class="order-img"
              />
              <div class="order-info">
                <h4>{{ order.houseName }}</h4>
                <p>{{ order.houseCity }} · {{ order.checkIn }} 至 {{ order.checkOut }} · {{ order.nights }} 晚</p>
                <p>{{ order.guests }} 位住客</p>
              </div>
              <div class="order-right">
                <el-tag :type="statusColors[order.status] as any">{{ order.statusName }}</el-tag>
                <div class="order-amount">&yen;{{ order.amount }}</div>
                <div class="order-no">{{ order.orderNo }}</div>
              </div>
            </div>
            <div class="order-actions" v-if="statusActions[order.status]" @click.stop>
              <el-button
                v-for="a in statusActions[order.status]"
                :key="a.action"
                :type="a.action === 'pay' ? 'primary' : a.action === 'cancel' ? 'danger' : 'success'"
                size="small"
                @click="doAction(order.orderNo, a.action)"
              >
                {{ a.label }}
              </el-button>
              <el-button size="small" @click="goDetail(order.orderNo)">详情</el-button>
            </div>
          </el-card>
        </div>

        <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />

        <div class="pagination-bar" v-if="total > 0">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="onPageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.orders-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  margin: 0 0 20px;
  font-size: 24px;
  font-weight: 800;
}

.order-tabs {
  margin-bottom: 20px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.order-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.order-top {
  display: flex;
  gap: 16px;
  align-items: center;
}

.order-img {
  width: 100px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-info h4 {
  margin: 0 0 4px;
  font-size: 15px;
}

.order-info p {
  margin: 2px 0;
  font-size: 13px;
  color: var(--c-muted);
}

.order-right {
  text-align: right;
  flex-shrink: 0;
}

.order-amount {
  font-size: 20px;
  font-weight: 700;
  color: var(--c-red);
  margin-top: 6px;
}

.order-no {
  font-size: 12px;
  color: var(--c-muted-light);
  margin-top: 2px;
}

.order-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--c-line);
  justify-content: flex-end;
}

@media (max-width: 640px) {
  .order-top {
    flex-direction: column;
  }

  .order-img {
    width: 100%;
    height: 160px;
  }

  .order-right {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
