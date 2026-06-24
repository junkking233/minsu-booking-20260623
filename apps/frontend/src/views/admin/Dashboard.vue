<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  House, Calendar, Bell, Money, Box, StarFilled, User, Collection
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { dashboardApi } from '@/api/dashboardApi';
import { orderApi } from '@/api/orderApi';
import { getCurrentUser } from '@/utils/auth';
import MetricCard from '@/components/common/MetricCard.vue';

const currentUser = getCurrentUser();

interface Overview {
  totalHouses: number; todayOrders: number; pendingOrders: number;
  totalRevenue: number; onShelfHouses: number; avgRating: number;
  totalUsers: number; totalFavorites: number;
}
interface TrendItem { date: string; count: number; }
interface PendingOrder {
  orderNo: string; houseName: string; guestName: string;
  amount: number; status: string; statusName: string; createTime: string;
}

const overview = ref<Overview>({
  totalHouses: 0, todayOrders: 0, pendingOrders: 0, totalRevenue: 0,
  onShelfHouses: 0, avgRating: 0, totalUsers: 0, totalFavorites: 0,
});
const trend = ref<TrendItem[]>([]);
const pendingOrders = ref<PendingOrder[]>([]);
const loading = ref(false);

async function loadDashboard() {
  loading.value = true;
  try {
    const [overviewData, trendData, ordersData] = await Promise.all([
      dashboardApi.overview(), dashboardApi.orderTrend(), dashboardApi.pendingOrders(),
    ]);
    overview.value = overviewData as Overview;
    trend.value = trendData as TrendItem[];
    pendingOrders.value = ordersData as PendingOrder[];
    renderChart();
  } catch { ElMessage.error('加载数据失败'); }
  finally { loading.value = false; }
}

async function handleConfirm(orderNo: string) {
  try {
    await orderApi.adminConfirm(orderNo);
    ElMessage.success('已确认'); loadDashboard();
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '操作失败'); }
}

function renderChart() {
  const container = document.getElementById('trend-chart');
  if (!container) return;
  const chart = echarts.init(container);
  const dates = trend.value.map((t) => t.date.slice(5));
  const counts = trend.value.map((t) => t.count);
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e5e8ec',
      textStyle: { color: '#1a2332' },
      boxShadow: '0 8px 24px rgb(26 35 50 / 12%)',
      extraCssText: 'border-radius: 10px; padding: 12px 16px;',
    },
    xAxis: {
      type: 'category', data: dates,
      axisLine: { lineStyle: { color: '#e5e8ec' } },
      axisTick: { show: false },
      axisLabel: { color: '#7a8899', fontSize: 12 },
    },
    yAxis: {
      type: 'value', minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f3f5f8' } },
      axisLabel: { color: '#7a8899', fontSize: 12 },
    },
    series: [{
      name: '订单数', type: 'bar',
      data: counts,
      barWidth: 36,
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#38bdf8' },
          { offset: 1, color: '#0284c7' },
        ]),
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#0ea5e9' },
            { offset: 1, color: '#0369a1' },
          ]),
        },
      },
    }],
    grid: { left: 44, right: 20, top: 16, bottom: 28 },
  });
  window.addEventListener('resize', () => chart.resize());
}

onMounted(loadDashboard);
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>欢迎回来，{{ currentUser?.name || currentUser?.username }}</h2>
        <p>今天是 {{ new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }) }}，您有 <strong class="text-warm">{{ overview.pendingOrders }}</strong> 个待处理订单</p>
      </div>
      <div class="welcome-decor">
        <svg width="100" height="80" viewBox="0 0 100 80" fill="none">
          <rect x="10" y="30" width="80" height="46" rx="8" stroke="var(--c-primary)" stroke-width="1.5" fill="var(--c-primary-bg)" opacity="0.8"/>
          <path d="M30 30V16l20 12 20-12v14" stroke="var(--c-primary)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" opacity="0.8"/>
          <rect x="40" y="38" width="20" height="4" rx="2" fill="var(--c-primary)" opacity="0.15"/>
          <rect x="40" y="46" width="14" height="3" rx="1.5" fill="var(--c-coral)" opacity="0.4"/>
        </svg>
      </div>
    </div>

    <!-- 指标卡片 -->
    <div class="metric-grid">
      <MetricCard label="总房源" :value="overview.totalHouses" :icon="House" color="primary" />
      <MetricCard label="今日订单" :value="overview.todayOrders" :icon="Calendar" color="coral" />
      <MetricCard label="待处理" :value="overview.pendingOrders" :icon="Bell" color="amber" />
      <MetricCard label="总收入" :value="overview.totalRevenue" :icon="Money" color="sage" prefix="¥" />
      <MetricCard label="上架房源" :value="overview.onShelfHouses" :icon="Box" color="blue" />
      <MetricCard label="平均评分" :value="overview.avgRating" :icon="StarFilled" color="amber" />
      <MetricCard label="注册用户" :value="overview.totalUsers" :icon="User" color="indigo" />
      <MetricCard label="收藏次数" :value="overview.totalFavorites" :icon="Collection" color="red" />
    </div>

    <div class="dash-grid">
      <!-- 趋势图 -->
      <div class="card-premium trend-card">
        <div class="card-header">
          <h3>近7日订单趋势</h3>
          <el-tag size="small" type="info">每日统计</el-tag>
        </div>
        <div id="trend-chart" class="chart-container" />
      </div>

      <!-- 待处理订单 -->
      <div class="card-premium pending-card">
        <div class="card-header">
          <h3>待处理订单</h3>
          <el-tag v-if="pendingOrders.length" size="small" type="danger">{{ pendingOrders.length }} 笔</el-tag>
        </div>
        <div v-if="pendingOrders.length > 0" class="pending-list">
          <div v-for="o in pendingOrders" :key="o.orderNo" class="pending-item">
            <div class="pending-left">
              <span class="pending-guest">{{ o.guestName }}</span>
              <span class="pending-house">{{ o.houseName }}</span>
              <span class="pending-time">{{ o.createTime?.replace('T', ' ').slice(0, 16) }}</span>
            </div>
            <div class="pending-right">
              <strong class="text-price">&yen;{{ o.amount }}</strong>
              <el-button v-if="o.status === 'PENDING'" type="primary" size="small" @click="handleConfirm(o.orderNo)">确认</el-button>
              <el-tag v-else size="small">{{ o.statusName }}</el-tag>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无待处理订单" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { max-width: 1400px; }

/* Welcome Banner */
.welcome-banner {
  display: flex; align-items: center; justify-content: space-between;
  padding: 24px 28px; margin-bottom: 24px;
  background: linear-gradient(135deg, var(--c-primary-bg) 0%, var(--c-bg-warm) 60%, var(--c-surface) 100%);
  border-radius: var(--radius-xl);
  border: 1px solid rgb(229 232 236 / 70%);
  box-shadow: var(--shadow-sm);
}
.welcome-text h2 {
  margin: 0 0 6px; font-size: 20px; font-weight: 700; color: var(--c-ink);
}
.welcome-text p { margin: 0; font-size: 14px; color: var(--c-muted); }
.welcome-decor { opacity: 0.6; }

/* Metric Grid */
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px; margin-bottom: 24px;
}

/* Chart & Pending Grid */
.dash-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px; align-items: start;
}

.card-header {
  display: flex; align-items: center; justify-content: space-between;
  gap: 12px; margin-bottom: 16px;
}
.card-header h3 {
  margin: 0; font-size: 17px; font-weight: 700; color: var(--c-ink);
}

.trend-card { padding: 24px; }
.chart-container { width: 100%; height: 300px; }

.pending-card { padding: 24px; }

.pending-list { display: flex; flex-direction: column; }
.pending-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 0; border-bottom: 1px solid var(--c-line-light);
}
.pending-item:last-child { border-bottom: none; }
.pending-left { display: flex; flex-direction: column; gap: 3px; }
.pending-guest { font-size: 14px; font-weight: 600; color: var(--c-ink); }
.pending-house { font-size: 13px; color: var(--c-body); }
.pending-time { font-size: 12px; color: var(--c-muted-light); }
.pending-right {
  display: flex; flex-direction: column; align-items: flex-end; gap: 6px;
}
.pending-right strong { font-size: 18px; }

@media (max-width: 1200px) {
  .metric-grid { grid-template-columns: repeat(2, 1fr); }
  .dash-grid { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .metric-grid { grid-template-columns: 1fr; }
  .welcome-banner { flex-direction: column; gap: 16px; }
}
</style>
