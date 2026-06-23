<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { dashboardApi } from '@/api/dashboardApi';
import { orderApi } from '@/api/orderApi';

interface Overview {
  totalHouses: number;
  todayOrders: number;
  pendingOrders: number;
  totalRevenue: number;
  onShelfHouses: number;
  avgRating: number;
  totalUsers: number;
  totalFavorites: number;
}

interface TrendItem {
  date: string;
  count: number;
}

interface PendingOrder {
  orderNo: string;
  houseName: string;
  guestName: string;
  amount: number;
  status: string;
  statusName: string;
  createTime: string;
}

const overview = ref<Overview>({
  totalHouses: 0, todayOrders: 0, pendingOrders: 0, totalRevenue: 0,
  onShelfHouses: 0, avgRating: 0, totalUsers: 0, totalFavorites: 0,
});
const trend = ref<TrendItem[]>([]);
const pendingOrders = ref<PendingOrder[]>([]);
const loading = ref(false);

const statusColors: Record<string, string> = {
  PENDING: 'warning', REFUNDING: 'warning',
};

async function loadDashboard() {
  loading.value = true;
  try {
    const [overviewData, trendData, ordersData] = await Promise.all([
      dashboardApi.overview(),
      dashboardApi.orderTrend(),
      dashboardApi.pendingOrders(),
    ]);
    overview.value = overviewData as Overview;
    trend.value = trendData as TrendItem[];
    pendingOrders.value = ordersData as PendingOrder[];
    renderChart();
  } catch (e) {
    ElMessage.error('加载数据失败');
  } finally {
    loading.value = false;
  }
}

async function handleConfirm(orderNo: string) {
  try {
    await orderApi.adminConfirm(orderNo);
    ElMessage.success('已确认');
    loadDashboard();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

function renderChart() {
  const container = document.getElementById('trend-chart');
  if (!container) return;
  const chart = echarts.init(container);
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: trend.value.map((t) => t.date.slice(5)),
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '订单数',
      type: 'bar',
      data: trend.value.map((t) => t.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#0ea5e9' },
          { offset: 1, color: '#2563eb' },
        ]),
        borderRadius: [6, 6, 0, 0],
      },
      barWidth: 36,
    }],
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
  });
  window.addEventListener('resize', () => chart.resize());
}

const metricCards = [
  { key: 'totalHouses', label: '总房源', icon: '🏠', color: '#0ea5e9' },
  { key: 'todayOrders', label: '今日订单', icon: '📋', color: '#f59e0b' },
  { key: 'pendingOrders', label: '待处理', icon: '⏳', color: '#f97316' },
  { key: 'totalRevenue', label: '总收入', icon: '💰', color: '#22c55e', prefix: '¥' },
  { key: 'onShelfHouses', label: '上架房源', icon: '📦', color: '#8b5cf6' },
  { key: 'avgRating', label: '平均评分', icon: '⭐', color: '#ec4899' },
  { key: 'totalUsers', label: '注册用户', icon: '👤', color: '#06b6d4' },
  { key: 'totalFavorites', label: '收藏次数', icon: '❤️', color: '#ef4444' },
];

onMounted(loadDashboard);
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <!-- 指标卡片 -->
    <div class="metric-grid">
      <div
        v-for="m in metricCards"
        :key="m.key"
        class="metric-card"
        :style="{ borderTopColor: m.color }"
      >
        <div class="metric-icon" :style="{ background: m.color + '18', color: m.color }">{{ m.icon }}</div>
        <div class="metric-body">
          <div class="metric-value">
            {{ m.prefix }}{{ m.key === 'totalRevenue' || m.key === 'avgRating' ? (overview as any)[m.key] : (overview as any)[m.key] }}
          </div>
          <div class="metric-label">{{ m.label }}</div>
        </div>
      </div>
    </div>

    <div class="dash-grid">
      <!-- 趋势图 -->
      <el-card class="trend-card">
        <h3>近7日订单趋势</h3>
        <div id="trend-chart" class="chart-container" />
      </el-card>

      <!-- 待处理订单 -->
      <el-card class="pending-card">
        <h3>待处理订单</h3>
        <div v-if="pendingOrders.length > 0">
          <div v-for="o in pendingOrders" :key="o.orderNo" class="pending-item">
            <div class="pending-info">
              <strong>{{ o.houseName }}</strong>
              <p>{{ o.guestName }} · ¥{{ o.amount }}</p>
              <p class="pending-time">{{ o.createTime?.replace('T', ' ') }}</p>
            </div>
            <div class="pending-right">
              <el-tag :type="statusColors[o.status] as any" size="small">{{ o.statusName }}</el-tag>
              <el-button v-if="o.status === 'PENDING'" type="primary" size="small" @click="handleConfirm(o.orderNo)">
                确认
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无待处理订单" :image-size="80" />
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1400px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-lg);
  border-top: 3px solid;
  box-shadow: var(--shadow-sm);
}

.metric-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-size: 22px;
  flex-shrink: 0;
}

.metric-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--c-ink);
}

.metric-label {
  font-size: 13px;
  color: var(--c-muted);
  margin-top: 2px;
}

.dash-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
  align-items: start;
}

.trend-card h3,
.pending-card h3 {
  margin: 0 0 16px;
  font-size: 17px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--c-line-light);
}

.pending-item:last-child {
  border-bottom: none;
}

.pending-info strong {
  font-size: 14px;
}

.pending-info p {
  margin: 3px 0 0;
  font-size: 13px;
  color: var(--c-muted);
}

.pending-time {
  font-size: 12px !important;
}

.pending-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

@media (max-width: 1200px) {
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dash-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
