<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, Star } from '@element-plus/icons-vue';
import { houseApi, type HouseSearchParams } from '@/api/houseApi';
import { configApi } from '@/api/configApi';
import { favoriteApi } from '@/api/favoriteApi';
import { getCurrentUser } from '@/utils/auth';

const route = useRoute();
const router = useRouter();
const currentUser = getCurrentUser();

interface CityItem { id: number; name: string; sort: number; }
interface HouseItem {
  id: number;
  name: string;
  city: string;
  price: number;
  type: string;
  capacity: number;
  coverImage: string;
  rating: number;
  sales: number;
  facilities: string[];
  available: boolean;
  availableReason: string | null;
}

const cities = ref<CityItem[]>([]);
const houses = ref<HouseItem[]>([]);
const total = ref(0);
const loading = ref(false);

const filters = reactive({
  keyword: '',
  city: '',
  type: '',
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  checkIn: '',
  checkOut: '',
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
  { label: '单间', value: '单间' },
  { label: '一居室', value: '一居室' },
  { label: '两居室', value: '两居室' },
  { label: '整套', value: '整套' },
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
    const params: HouseSearchParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    };
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
  } finally {
    loading.value = false;
  }
}

function goDetail(id: number) {
  const query: Record<string, string> = {};
  if (filters.checkIn) query.checkIn = filters.checkIn;
  if (filters.checkOut) query.checkOut = filters.checkOut;
  router.push({ path: `/portal/houses/${id}`, query });
}

function goBooking(id: number) {
  if (!currentUser) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  const query: Record<string, string> = {};
  if (filters.checkIn) query.checkIn = filters.checkIn;
  if (filters.checkOut) query.checkOut = filters.checkOut;
  if (filters.guests) query.guests = String(filters.guests);
  router.push({ path: `/portal/booking/${id}`, query });
}

async function toggleFavorite(house: HouseItem & { _favorited?: boolean }) {
  if (!currentUser) {
    ElMessage.warning('请先登录');
    return;
  }
  try {
    if (house._favorited) {
      await favoriteApi.remove(house.id);
      house._favorited = false;
      ElMessage.success('已取消收藏');
    } else {
      await favoriteApi.add(house.id);
      house._favorited = true;
      ElMessage.success('收藏成功');
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

function onPageChange(page: number) {
  pagination.page = page;
  search();
}

function onReset() {
  filters.keyword = '';
  filters.city = '';
  filters.type = '';
  filters.minPrice = undefined;
  filters.maxPrice = undefined;
  filters.checkIn = '';
  filters.checkOut = '';
  filters.guests = undefined;
  filters.sort = 'recommend';
  pagination.page = 1;
  search();
}

watch(() => filters.sort, () => { pagination.page = 1; search(); });

onMounted(async () => {
  try {
    const data = await configApi.cities() as CityItem[];
    cities.value = data;
  } catch { /* ignore */ }
  parseQuery();
  search();
});
</script>

<template>
  <div class="house-list-page">
    <div class="page-inner">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索房源名称/城市/设施"
          :prefix-icon="Search"
          clearable
          class="filter-item"
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="filters.city" placeholder="城市" clearable class="filter-item filter-short" @change="search">
          <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.name" />
        </el-select>
        <el-select v-model="filters.type" placeholder="房型" clearable class="filter-item filter-short" @change="search">
          <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-date-picker v-model="filters.checkIn" type="date" placeholder="入住" class="filter-item filter-date" @change="search" />
        <el-date-picker v-model="filters.checkOut" type="date" placeholder="离店" class="filter-item filter-date" @change="search" />
        <el-input-number v-model="filters.guests" :min="1" placeholder="人数" class="filter-item filter-num" @change="search" />
        <el-select v-model="filters.sort" class="filter-item filter-short">
          <el-option v-for="s in sortOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button @click="onReset">重置</el-button>
        <el-button type="primary" @click="search">搜索</el-button>
      </div>

      <!-- 房源列表 -->
      <div class="house-grid" v-loading="loading">
        <el-card
          v-for="house in houses"
          :key="house.id"
          class="house-card"
          shadow="hover"
          @click="goDetail(house.id)"
        >
          <div class="card-cover">
            <img :src="house.coverImage || '/placeholder.svg'" :alt="house.name" />
            <span class="card-type">{{ house.type }}</span>
            <span class="card-available" v-if="filters.checkIn && !house.available">不可订</span>
          </div>
          <div class="card-body">
            <div class="card-title">{{ house.name }}</div>
            <div class="card-city">{{ house.city }}</div>
            <div class="card-meta">
              <span class="card-rating"><el-icon><Star /></el-icon>{{ house.rating }}</span>
              <span>可住 {{ house.capacity }} 人</span>
            </div>
            <div class="card-bottom">
              <span class="card-price"><strong>&yen;{{ house.price }}</strong>/晚</span>
              <div class="card-actions" @click.stop>
                <el-button
                  :type="(house as any)._favorited ? 'warning' : 'default'"
                  size="small"
                  circle
                  @click="toggleFavorite(house as any)"
                >
                  {{ (house as any)._favorited ? '♥' : '♡' }}
                </el-button>
                <el-button type="primary" size="small" :disabled="filters.checkIn && !house.available" @click="goBooking(house.id)">
                  预约
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <el-empty v-if="!loading && houses.length === 0" description="未找到房源" />

      <!-- 分页 -->
      <div class="pagination-bar" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.house-list-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 20px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 16px;
  background: var(--c-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  margin-bottom: 24px;
}

.filter-item {
  width: 160px;
}

.filter-short {
  width: 120px;
}

.filter-date {
  width: 140px;
}

.filter-num {
  width: 100px;
}

.house-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.house-card {
  cursor: pointer;
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-base);
}

.house-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.house-card:hover .card-cover img {
  transform: scale(1.05);
}

.card-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.card-type {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  background: rgb(0 0 0 / 50%);
  color: #fff;
  font-size: 11px;
  border-radius: var(--radius-sm);
}

.card-available {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 10px;
  background: var(--c-red);
  color: #fff;
  font-size: 11px;
  border-radius: var(--radius-sm);
}

.card-body {
  padding: 12px 14px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-city {
  font-size: 13px;
  color: var(--c-muted);
  margin: 4px 0 8px;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--c-muted);
  margin-bottom: 10px;
}

.card-rating {
  color: var(--c-amber);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 3px;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--c-line);
}

.card-price strong {
  font-size: 18px;
  color: var(--c-red);
  font-weight: 700;
}

.card-price {
  font-size: 12px;
  color: var(--c-muted);
}

.card-actions {
  display: flex;
  gap: 6px;
  align-items: center;
}

@media (max-width: 1024px) {
  .house-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .house-grid {
    grid-template-columns: 1fr;
  }

  .filter-bar {
    flex-direction: column;
  }

  .filter-item, .filter-short, .filter-date, .filter-num {
    width: 100%;
  }
}
</style>
