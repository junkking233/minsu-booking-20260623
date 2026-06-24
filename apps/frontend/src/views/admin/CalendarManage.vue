<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import { houseApi } from '@/api/houseApi';
import { availabilityApi } from '@/api/availabilityApi';

const route = useRoute();
const router = useRouter();
const houseId = Number(route.params.houseId);

interface HouseOption { id: number; name: string; }
interface DayItem { date: string; status: string; price: number; orderNo?: string; }

const houses = ref<HouseOption[]>([]);
const selectedHouseId = ref(houseId);
const houseName = ref('');
const days = ref<DayItem[]>([]);
const loading = ref(false);

const currentMonth = ref(new Date().toISOString().slice(0, 7));

const monthLabel = computed(() => {
  const [y, m] = currentMonth.value.split('-');
  return `${y}年${parseInt(m)}月`;
});

async function loadHouses() {
  try {
    const data = await houseApi.adminList({ pageSize: 100 }) as { records: HouseOption[] };
    houses.value = data.records || [];
    // 如果当前选择的房源不在列表中，自动选第一个
    if (houses.value.length > 0 && !houses.value.find(h => h.id === selectedHouseId.value)) {
      selectedHouseId.value = houses.value[0].id;
      router.replace(`/admin/calendar/${selectedHouseId.value}`);
    }
  } catch { /* ignore */ }
}

async function loadCalendar() {
  loading.value = true;
  try {
    const data = await availabilityApi.getCalendar(selectedHouseId.value, currentMonth.value) as { houseName: string; days: DayItem[] };
    houseName.value = data.houseName || '';
    days.value = data.days || [];
  } catch (e) {
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
}

function onHouseChange() {
  router.replace(`/admin/calendar/${selectedHouseId.value}`);
  loadCalendar();
}

function prevMonth() {
  const [y, m] = currentMonth.value.split('-').map(Number);
  if (m <= 1) {
    currentMonth.value = `${y - 1}-12`;
  } else {
    currentMonth.value = `${y}-${String(m - 1).padStart(2, '0')}`;
  }
  loadCalendar();
}

function nextMonth() {
  const [y, m] = currentMonth.value.split('-').map(Number);
  if (m >= 12) {
    currentMonth.value = `${y + 1}-01`;
  } else {
    currentMonth.value = `${y}-${String(m + 1).padStart(2, '0')}`;
  }
  loadCalendar();
}

async function toggleDay(date: string, status: string) {
  if (status === 'BOOKED') {
    ElMessage.warning('已订日期不能直接修改');
    return;
  }
  try {
    await availabilityApi.toggle(selectedHouseId.value, date);
    ElMessage.success('操作成功');
    loadCalendar();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

const statusColors: Record<string, { bg: string; text: string; label: string }> = {
  AVAILABLE: { bg: '#f0fdf4', text: '#166534', label: '可订' },
  LOCKED: { bg: '#fef2f2', text: '#991b1b', label: '锁定' },
  BOOKED: { bg: '#eff6ff', text: '#1e40af', label: '已订' },
};

onMounted(async () => {
  await loadHouses();
  loadCalendar();
});
</script>

<template>
  <div class="calendar-page">
    <div class="page-header">
      <div class="header-left">
        <h2>房态维护</h2>
        <el-select v-model="selectedHouseId" placeholder="选择房源" class="house-select" @change="onHouseChange">
          <el-option v-for="h in houses" :key="h.id" :label="h.name" :value="h.id" />
        </el-select>
      </div>
      <el-button @click="router.push('/admin/houses')">返回房源管理</el-button>
    </div>

    <div class="calendar-header" v-if="houseName">
      <el-button :icon="ArrowLeft" @click="prevMonth" />
      <span class="month-label">{{ monthLabel }}</span>
      <el-button :icon="ArrowRight" @click="nextMonth" />
      <span class="house-label">{{ houseName }}</span>
    </div>

    <div v-loading="loading">
      <!-- 统计条 -->
      <div class="stats-strip" v-if="days.length">
        <div class="stat-pill stat-available">{{ days.filter(d => d.status === 'AVAILABLE').length }} 天可订</div>
        <div class="stat-pill stat-booked">{{ days.filter(d => d.status === 'BOOKED').length }} 天已订</div>
        <div class="stat-pill stat-locked">{{ days.filter(d => d.status === 'LOCKED').length }} 天锁定</div>
      </div>

      <!-- 图例 -->
      <div class="legend">
        <div v-for="(v, k) in statusColors" :key="k" class="legend-item">
          <span class="legend-dot" :style="{ background: v.text }" />
          {{ v.label }}
        </div>
      </div>

      <!-- 星期头部 -->
      <div class="weekday-row" v-if="days.length">
        <div v-for="w in ['一','二','三','四','五','六','日']" :key="w" class="weekday-cell">{{ w }}</div>
      </div>

      <!-- 日历网格 -->
      <div class="calendar-grid" v-if="days.length > 0">
        <div
          v-for="day in days"
          :key="day.date"
          class="calendar-day"
          :data-status="day.status"
          :class="{ clickable: day.status !== 'BOOKED', today: day.date === new Date().toISOString().slice(0, 10) }"
          @click="toggleDay(day.date, day.status)"
        >
          <div class="day-num">{{ parseInt(day.date.slice(8)) }}</div>
          <div class="day-status-badge">{{ statusColors[day.status]?.label }}</div>
          <div class="day-price">¥{{ day.price }}</div>
        </div>
      </div>

      <el-empty v-else description="暂无数据" />
    </div>
  </div>
</template>

<style scoped>
.calendar-page {
  max-width: 1000px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.house-select {
  width: 200px;
}

.calendar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.month-label {
  font-size: 18px;
  font-weight: 700;
  min-width: 120px;
  text-align: center;
}

.house-label {
  margin-left: 20px;
  color: var(--c-muted);
}

.stats-strip {
  display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap;
}
.stat-pill {
  padding: 6px 16px; border-radius: var(--radius-full);
  font-size: 13px; font-weight: 600;
}
.stat-available { background: var(--c-green-bg); color: var(--c-green); }
.stat-booked { background: var(--c-blue-bg); color: var(--c-blue); }
.stat-locked { background: var(--c-red-bg); color: var(--c-red); }

.legend {
  display: flex; gap: 24px; margin-bottom: 16px; flex-wrap: wrap;
}
.legend-item {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: var(--c-muted);
}
.legend-dot { width: 10px; height: 10px; border-radius: 3px; }

.weekday-row {
  display: grid; grid-template-columns: repeat(7, 1fr);
  gap: 8px; margin-bottom: 8px;
}
.weekday-cell {
  text-align: center; font-size: 13px; font-weight: 600;
  color: var(--c-muted); padding: 6px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-day {
  padding: 14px 8px 10px;
  text-align: center;
  border: 1.5px solid transparent;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
  min-height: 80px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  background: var(--c-surface);
}
.calendar-day[data-status='AVAILABLE'] {
  background: #f0fdf4; border-color: #bbf7d0;
}
.calendar-day[data-status='LOCKED'] {
  background: #fef2f2; border-color: #fecaca;
}
.calendar-day[data-status='BOOKED'] {
  background: #eff6ff; border-color: #bfdbfe;
}
.calendar-day.today { box-shadow: 0 0 0 2px var(--c-primary), 0 0 0 4px rgb(2 132 199 / 15%); }
.calendar-day.clickable { cursor: pointer; }
.calendar-day.clickable:hover { transform: scale(1.04); box-shadow: var(--shadow-md); }

.day-num { font-size: 18px; font-weight: 700; color: var(--c-ink); }
.calendar-day[data-status='AVAILABLE'] .day-num { color: var(--c-green); }
.calendar-day[data-status='BOOKED'] .day-num { color: var(--c-blue); }
.calendar-day[data-status='LOCKED'] .day-num { color: var(--c-red); }

.day-status-badge {
  font-size: 11px; font-weight: 600; margin-top: 4px;
  padding: 1px 8px; border-radius: var(--radius-full);
}
.calendar-day[data-status='AVAILABLE'] .day-status-badge { background: #dcfce7; color: var(--c-green); }
.calendar-day[data-status='BOOKED'] .day-status-badge { background: #dbeafe; color: var(--c-blue); }
.calendar-day[data-status='LOCKED'] .day-status-badge { background: #fee2e2; color: var(--c-red); }

.day-price { font-size: 11px; margin-top: 4px; color: var(--c-muted); }
</style>
