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
  const d = new Date(y, m - 2, 1);
  currentMonth.value = d.toISOString().slice(0, 7);
  loadCalendar();
}

function nextMonth() {
  const [y, m] = currentMonth.value.split('-').map(Number);
  const d = new Date(y, m, 1);
  currentMonth.value = d.toISOString().slice(0, 7);
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
      <!-- 图例 -->
      <div class="legend">
        <div v-for="(v, k) in statusColors" :key="k" class="legend-item">
          <span class="legend-dot" :style="{ background: v.text }" />
          {{ v.label }}
        </div>
      </div>

      <!-- 日历网格 -->
      <div class="calendar-grid" v-if="days.length > 0">
        <div
          v-for="day in days"
          :key="day.date"
          class="calendar-day"
          :style="{ background: statusColors[day.status]?.bg, color: statusColors[day.status]?.text, borderColor: statusColors[day.status]?.text + '30' }"
          :class="{ clickable: day.status !== 'BOOKED' }"
          @click="toggleDay(day.date, day.status)"
        >
          <div class="day-num">{{ parseInt(day.date.slice(8)) }}</div>
          <div class="day-status">{{ statusColors[day.status]?.label }}</div>
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

.legend {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--c-muted);
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-day {
  padding: 12px 8px;
  text-align: center;
  border: 1px solid;
  border-radius: var(--radius-sm);
  transition: transform var(--transition-fast);
}

.calendar-day.clickable {
  cursor: pointer;
}

.calendar-day.clickable:hover {
  transform: scale(1.05);
}

.day-num {
  font-size: 18px;
  font-weight: 700;
}

.day-status {
  font-size: 12px;
  margin-top: 2px;
}

.day-price {
  font-size: 11px;
  margin-top: 2px;
  opacity: 0.7;
}
</style>
