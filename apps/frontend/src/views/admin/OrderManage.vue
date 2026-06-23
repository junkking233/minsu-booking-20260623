<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { orderApi } from '@/api/orderApi';

interface OrderRecord {
  orderNo: string;
  houseName: string;
  houseCity: string;
  guestName: string;
  phone: string;
  checkIn: string;
  checkOut: string;
  nights: number;
  amount: number;
  status: string;
  statusName: string;
  createTime: string;
}

const orders = ref<OrderRecord[]>([]);
const total = ref(0);
const loading = ref(false);

const filters = reactive({ keyword: '', status: '' });
const pagination = reactive({ page: 1, pageSize: 20 });

const statusOptions = [
  '', 'PENDING', 'WAIT_PAY', 'BOOKED', 'CHECKED_IN', 'COMPLETED',
  'REVIEWED', 'CANCELLED', 'REFUNDING', 'REFUNDED',
];
const statusLabels: Record<string, string> = {
  '': '全部', PENDING: '待确认', WAIT_PAY: '待支付', BOOKED: '已预订',
  CHECKED_IN: '入住中', COMPLETED: '已完成', REVIEWED: '已评价',
  CANCELLED: '已取消', REFUNDING: '退款中', REFUNDED: '已退款',
};
const statusColors: Record<string, string> = {
  PENDING: 'warning', WAIT_PAY: 'danger', BOOKED: '', CHECKED_IN: 'success',
  COMPLETED: 'info', REVIEWED: 'success', CANCELLED: 'info', REFUNDING: 'warning', REFUNDED: 'info',
};

// Detail dialog
const detailVisible = ref(false);
const detailOrder = ref<OrderRecord | null>(null);

async function loadOrders() {
  loading.value = true;
  try {
    const data = await orderApi.adminList({ ...filters, page: pagination.page, pageSize: pagination.pageSize }) as { records: OrderRecord[]; total: number };
    orders.value = data.records || [];
    total.value = data.total || 0;
  } catch (e) {
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  pagination.page = 1;
  loadOrders();
}

function onReset() {
  filters.keyword = '';
  filters.status = '';
  onSearch();
}

async function doAction(orderNo: string, action: string, label: string) {
  try {
    await ElMessageBox.confirm(`确定执行「${label}」操作？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    const apiMap: Record<string, () => Promise<any>> = {
      confirm: () => orderApi.adminConfirm(orderNo),
      reject: () => orderApi.adminReject(orderNo),
      checkin: () => orderApi.adminCheckin(orderNo),
      complete: () => orderApi.adminComplete(orderNo),
      refund: () => orderApi.adminRefund(orderNo),
      refundApprove: () => orderApi.adminRefundApprove(orderNo),
      refundReject: () => orderApi.adminRefundReject(orderNo),
    };
    await apiMap[action]();
    ElMessage.success('操作成功');
    loadOrders();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

function showDetail(row: OrderRecord) {
  detailOrder.value = row;
  detailVisible.value = true;
}

function getActions(status: string) {
  switch (status) {
    case 'PENDING': return [
      { action: 'confirm', label: '确认', type: 'success' },
      { action: 'reject', label: '拒绝', type: 'danger' },
    ];
    case 'BOOKED': return [
      { action: 'checkin', label: '入住', type: '' },
      { action: 'refund', label: '退款', type: 'warning' },
    ];
    case 'CHECKED_IN': return [
      { action: 'complete', label: '完成', type: 'success' },
    ];
    case 'REFUNDING': return [
      { action: 'refundApprove', label: '同意退款', type: 'success' },
      { action: 'refundReject', label: '拒绝退款', type: 'danger' },
    ];
    default: return [];
  }
}

onMounted(loadOrders);
</script>

<template>
  <div class="order-manage">
    <!-- 工具栏 -->
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-input v-model="filters.keyword" placeholder="订单号/房源/用户/手机号" :prefix-icon="Search" clearable class="w-240" @keyup.enter="onSearch" @clear="onSearch" />
        <el-select v-model="filters.status" placeholder="状态" clearable class="w-140" @change="onSearch">
          <el-option v-for="s in statusOptions" :key="s" :label="statusLabels[s]" :value="s" />
        </el-select>
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="onReset">重置</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table :data="orders" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="houseName" label="房源" min-width="130" />
      <el-table-column prop="guestName" label="用户" width="80" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column label="日期" width="200">
        <template #default="{ row }">{{ row.checkIn }} ~ {{ row.checkOut }}</template>
      </el-table-column>
      <el-table-column label="金额" width="100">
        <template #default="{ row }">&yen;{{ row.amount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusColors[row.status] as any" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">详情</el-button>
          <template v-for="a in getActions(row.status)" :key="a.action">
            <el-button size="small" :type="a.type as any" @click="doAction(row.orderNo, a.action, a.label)">{{ a.label }}</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadOrders"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="500px">
      <div v-if="detailOrder" class="detail-body">
        <div class="detail-row"><label>订单号</label><span>{{ detailOrder.orderNo }}</span></div>
        <div class="detail-row"><label>房源</label><span>{{ detailOrder.houseName }}</span></div>
        <div class="detail-row"><label>城市</label><span>{{ detailOrder.houseCity }}</span></div>
        <div class="detail-row"><label>联系人</label><span>{{ detailOrder.guestName }}</span></div>
        <div class="detail-row"><label>手机号</label><span>{{ detailOrder.phone }}</span></div>
        <div class="detail-row"><label>入住日期</label><span>{{ detailOrder.checkIn }} ~ {{ detailOrder.checkOut }}</span></div>
        <div class="detail-row"><label>晚数</label><span>{{ detailOrder.nights }} 晚</span></div>
        <div class="detail-row"><label>金额</label><strong>&yen;{{ detailOrder.amount }}</strong></div>
        <div class="detail-row"><label>状态</label><el-tag :type="statusColors[detailOrder.status] as any" size="small">{{ detailOrder.statusName }}</el-tag></div>
        <div class="detail-row"><label>创建时间</label><span>{{ detailOrder.createTime?.replace('T', ' ') }}</span></div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-manage {
  max-width: 1400px;
}

.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.w-240 { width: 240px; }
.w-140 { width: 140px; }

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.detail-row label {
  width: 70px;
  color: var(--c-muted);
  flex-shrink: 0;
}

.detail-row strong {
  color: var(--c-red);
  font-size: 18px;
}
</style>
