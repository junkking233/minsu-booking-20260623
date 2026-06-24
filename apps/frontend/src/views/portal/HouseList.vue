<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, Refresh } from '@element-plus/icons-vue';
import { houseApi, type HouseSearchParams } from '@/api/houseApi';
import { configApi } from '@/api/configApi';
import { favoriteApi } from '@/api/favoriteApi';
import { getCurrentUser } from '@/utils/auth';
import HouseCard from '@/components/common/HouseCard.vue';
import EmptyState from '@/components/common/EmptyState.vue';

const route = useRoute();
const router = useRouter();
const currentUser = getCurrentUser();

interface CityItem { id: number; name: string; sort: number; }
interface HouseItem {
  id: number; name: string; city: string; price: number; type: string;
  capacity: number; coverImage: string; rating: number; sales: number;
  facilities: string[]; available: boolean; availableReason: string | null;
  _favorited?: boolean;
}

const cities = ref<CityItem[]>([]);
const houses = ref<HouseItem[]>([]);
const total = ref(0);
const loading = ref(false);

const filters = reactive({
  keyword: '', city: '', type: '',
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  checkIn: '', checkOut: '',
  guests: undefined as number | undefined,
  sort: 'recommend',
});
const pagination = reactive({ page: 1, pageSize: 20 });

const sortOptions = [
  { label: '推荐排序', value: 'recommend' },
  { label: '价格从低到高', value: 'price_asc' },
  { label: '评分最高', value: 'rating' },
  { label: '销量最高', value: 'sales' },
];
const typeOptions = [
  { label: '单间', value: '单间' }, { label: '一居室', value: '一居室' },
  { label: '两居室', value: '两居室' }, { label: '整套', value: '整套' },
];

function parseQuery() {
  const q = route.query;
  if (q.city) filters.city = String(q.city);
  if (q.checkIn) filters.checkIn = String(q.checkIn);
  if (q.checkOut) filters.checkOut = String(q.checkOut);
  if (q.guests) filters.guests = Number(q.guests);
  if (q.keyword) filters.keyword = String(q.keyword);
}

async function search() {
  loading.value = true;
  try {
    const params: HouseSearchParams = { page: pagination.page, pageSize: pagination.pageSize };
    if (filters.keyword) params.keyword = filters.keyword;
    if (filters.city) params.city = filters.city;
    if (filters.type) params.type = filters.type;
    if (filters.minPrice != null) params.minPrice = filters.minPrice;
    if (filters.maxPrice != null) params.maxPrice = filters.maxPrice;
    if (filters.checkIn) params.checkIn = filters.checkIn;
    if (filters.checkOut) params.checkOut = filters.checkOut;
    if (filters.guests) params.guests = filters.guests;
    if (filters.sort) params.sort = filters.sort;
    const data = await houseApi.search(params) as { records: HouseItem[]; total: number };
    houses.value = data.records || [];
    total.value = data.total || 0;
  } catch (e) {
    ElMessage.error('搜索失败');
  } finally { loading.value = false; }
}

function goDetail(id: number | string) {
  const query: Record<string, string> = {};
  if (filters.checkIn) query.checkIn = filters.checkIn;
  if (filters.checkOut) query.checkOut = filters.checkOut;
  router.push({ path: `/portal/houses/${id}`, query });
}
function goBooking(house: any) {
  if (!currentUser) { ElMessage.warning('请先登录'); router.push('/login'); return; }
  const query: Record<string, string> = {};
  if (filters.checkIn) query.checkIn = filters.checkIn;
  if (filters.checkOut) query.checkOut = filters.checkOut;
  if (filters.guests) query.guests = String(filters.guests);
  router.push({ path: `/portal/booking/${house.id}`, query });
}
async function toggleFavorite(house: HouseItem, cur: boolean) {
  if (!currentUser) { ElMessage.warning('请先登录'); return; }
  try {
    if (cur) { await favoriteApi.remove(house.id); house._favorited = false; ElMessage.success('已取消收藏'); }
    else { await favoriteApi.add(house.id); house._favorited = true; ElMessage.success('收藏成功'); }
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '操作失败'); }
}
function onPageChange(page: number) { pagination.page = page; search(); }
function onReset() {
  filters.keyword = ''; filters.city = ''; filters.type = ''; filters.minPrice = undefined;
  filters.maxPrice = undefined; filters.checkIn = ''; filters.checkOut = '';
  filters.guests = undefined; filters.sort = 'recommend'; pagination.page = 1; search();
}
watch(() => filters.sort, () => { pagination.page = 1; search(); });

onMounted(async () => {
  try { cities.value = (await configApi.cities()) as CityItem[]; } catch { /* ignore */ }
  parseQuery(); search();
});
</script>

<template>
  <div class="house-list-page">
    <div class="page-inner">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="filter-row">
          <el-input v-model="filters.keyword" placeholder="搜索房源名称/城市/设施" :prefix-icon="Search" clearable class="filter-keyword" @keyup.enter="search" @clear="search" />
          <el-select v-model="filters.city" placeholder="城市" clearable class="filter-sel" @change="search">
            <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.name" />
          </el-select>
          <el-select v-model="filters.type" placeholder="房型" clearable class="filter-sel" @change="search">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
          <el-date-picker v-model="filters.checkIn" type="date" placeholder="入住" class="filter-date" @change="search" />
          <el-date-picker v-model="filters.checkOut" type="date" placeholder="离店" class="filter-date" @change="search" />
          <el-input-number v-model="filters.guests" :min="1" placeholder="人数" class="filter-num" @change="search" />
          <el-select v-model="filters.sort" class="filter-sort">
            <el-option v-for="s in sortOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
          <div class="filter-actions">
            <el-button text @click="onReset"><el-icon><Refresh /></el-icon>重置</el-button>
            <el-button type="primary" @click="search"><el-icon><Search /></el-icon>搜索</el-button>
          </div>
        </div>
      </div>

      <!-- 结果计数 -->
      <div class="result-count" v-if="total > 0">共找到 <strong>{{ total }}</strong> 套房源</div>

      <!-- 房源列表 -->
      <div class="house-grid" v-if="houses.length > 0">
        <HouseCard
          v-for="house in houses"
          :key="house.id"
          size="small"
          :house="{
            id: house.id, name: house.name, city: house.city,
            price: house.price, rating: house.rating, type: house.type,
            capacity: house.capacity, coverImage: house.coverImage,
            sales: house.sales,
          }"
          :favorited="!!house._favorited"
          @click="goDetail"
          @book="goBooking"
          @toggle-favorite="(h: any, cur: boolean) => toggleFavorite(house, cur)"
        />
      </div>

      <EmptyState v-else-if="!loading" type="search" title="未找到房源" description="试试调整筛选条件或换个关键词" />

      <!-- 分页 -->
      <div class="pagination-bar" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.page" :page-size="pagination.pageSize"
          :total="total" layout="total, prev, pager, next" @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.house-list-page { max-width: 1280px; margin: 0 auto; padding: 24px 20px; }

.filter-bar {
  padding: 16px 20px;
  background: var(--c-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid rgb(229 232 236 / 70%);
  margin-bottom: 20px;
}
.filter-row { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.filter-keyword { width: 200px; }
.filter-sel { width: 120px; min-width: 100px; }
.filter-date { width: 140px; min-width: 120px; }
.filter-num { width: 100px; min-width: 90px; }
.filter-sort { width: 140px; min-width: 120px; }
.filter-actions { display: flex; gap: 8px; margin-left: auto; }

.result-count { margin-bottom: 16px; font-size: 14px; color: var(--c-muted); }
.result-count strong { color: var(--c-ink); font-weight: 600; }

.house-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1024px) { .house-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px) {
  .house-grid { grid-template-columns: 1fr; }
  .filter-row { flex-direction: column; }
  .filter-keyword, .filter-sel, .filter-date, .filter-num, .filter-sort { width: 100%; }
  .filter-actions { margin-left: 0; width: 100%; justify-content: flex-end; }
}
</style>
