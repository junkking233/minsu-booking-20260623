<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, Star } from '@element-plus/icons-vue';
import { houseApi } from '@/api/houseApi';
import { configApi } from '@/api/configApi';

const router = useRouter();

interface CityItem {
  id: number;
  name: string;
  sort: number;
}

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
}

const cities = ref<CityItem[]>([]);
const recommendHouses = ref<HouseItem[]>([]);
const loading = ref(false);

const searchForm = ref({
  city: '',
  checkIn: '',
  checkOut: '',
  guests: 1,
});

function onSearch() {
  const query: Record<string, string> = {};
  if (searchForm.value.city) query.city = searchForm.value.city;
  if (searchForm.value.checkIn) query.checkIn = searchForm.value.checkIn;
  if (searchForm.value.checkOut) query.checkOut = searchForm.value.checkOut;
  if (searchForm.value.guests > 1) query.guests = String(searchForm.value.guests);
  router.push({ path: '/portal/houses', query });
}

function goCity(city: string) {
  router.push({ path: '/portal/houses', query: { city } });
}

function goDetail(id: number) {
  router.push(`/portal/houses/${id}`);
}

async function loadData() {
  loading.value = true;
  try {
    const [citiesData, housesData] = await Promise.all([
      configApi.cities(),
      houseApi.search({ pageSize: 3, sort: 'recommend' }),
    ]);
    cities.value = citiesData as CityItem[];
    const housesResult = housesData as { records: HouseItem[] };
    recommendHouses.value = housesResult.records || [];
  } catch (e) {
    ElMessage.error('加载数据失败');
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>

<template>
  <div class="home-page" v-loading="loading">
    <!-- 搜索区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1>发现旅途中的家</h1>
        <p>精选品质民宿，开启美好旅程</p>
        <div class="search-bar">
          <el-select
            v-model="searchForm.city"
            placeholder="选择城市"
            clearable
            class="search-item"
            size="large"
          >
            <el-option
              v-for="c in cities"
              :key="c.id"
              :label="c.name"
              :value="c.name"
            />
          </el-select>
          <el-date-picker
            v-model="searchForm.checkIn"
            type="date"
            placeholder="入住日期"
            :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000"
            size="large"
            class="search-item"
          />
          <el-date-picker
            v-model="searchForm.checkOut"
            type="date"
            placeholder="离店日期"
            :disabled-date="(d: Date) => !searchForm.checkIn || d.getTime() <= new Date(searchForm.checkIn).getTime()"
            size="large"
            class="search-item"
          />
          <el-input-number
            v-model="searchForm.guests"
            :min="1"
            :max="20"
            size="large"
            class="search-item search-guests"
          />
          <el-button type="primary" size="large" :icon="Search" class="search-btn" @click="onSearch">
            搜索房源
          </el-button>
        </div>
      </div>
    </section>

    <!-- 热门城市 -->
    <section class="section" v-if="cities.length > 0">
      <h2 class="section-title">热门城市</h2>
      <div class="city-tags">
        <el-tag
          v-for="c in cities"
          :key="c.id"
          size="large"
          class="city-tag"
          @click="goCity(c.name)"
        >
          {{ c.name }}
        </el-tag>
      </div>
    </section>

    <!-- 推荐房源 -->
    <section class="section">
      <h2 class="section-title">推荐房源</h2>
      <div class="house-grid" v-if="recommendHouses.length > 0">
        <el-card
          v-for="house in recommendHouses"
          :key="house.id"
          class="house-card"
          shadow="hover"
          @click="goDetail(house.id)"
        >
          <div class="card-cover">
            <img
              :src="house.coverImage || '/placeholder.svg'"
              :alt="house.name"
            />
            <span class="card-type">{{ house.type }}</span>
          </div>
          <div class="card-body">
            <div class="card-title">{{ house.name }}</div>
            <div class="card-city">{{ house.city }}</div>
            <div class="card-meta">
              <span class="card-rating">
                <el-icon><Star /></el-icon>
                {{ house.rating }}
              </span>
              <span class="card-capacity">可住 {{ house.capacity }} 人</span>
            </div>
            <div class="card-bottom">
              <span class="card-price">
                <strong>&yen;{{ house.price }}</strong>/晚
              </span>
            </div>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无推荐房源" />
    </section>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Hero */
.hero-section {
  padding: 60px 0 40px;
  text-align: center;
}

.hero-content h1 {
  margin: 0;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -1px;
  color: var(--c-ink);
}

.hero-content p {
  margin: 12px 0 32px;
  color: var(--c-muted);
  font-size: 17px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 880px;
  margin: 0 auto;
  padding: 12px;
  background: var(--c-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  flex-wrap: wrap;
  justify-content: center;
}

.search-item {
  width: 150px;
}

.search-guests {
  width: 100px;
}

.search-btn {
  height: 40px;
  border-radius: var(--radius-md);
  font-weight: 600;
}

/* Sections */
.section {
  margin: 40px 0;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 20px;
  color: var(--c-ink);
}

/* City tags */
.city-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.city-tag {
  cursor: pointer;
  font-size: 14px;
  padding: 8px 20px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.city-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgb(14 165 233 / 20%);
}

/* House grid */
.house-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.house-card {
  cursor: pointer;
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: transform var(--transition-fast);
}

.house-card:hover {
  transform: translateY(-4px);
}

.card-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-type {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  background: rgb(0 0 0 / 50%);
  color: #fff;
  font-size: 12px;
  border-radius: var(--radius-sm);
  backdrop-filter: blur(4px);
}

.card-body {
  padding: 14px 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-ink);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-city {
  font-size: 13px;
  color: var(--c-muted);
  margin-bottom: 10px;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
}

.card-rating {
  color: var(--c-amber);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 3px;
}

.card-capacity {
  color: var(--c-muted);
}

.card-bottom {
  padding-top: 12px;
  border-top: 1px solid var(--c-line);
}

.card-price strong {
  font-size: 20px;
  color: var(--c-red);
  font-weight: 700;
}

.card-price {
  color: var(--c-muted);
  font-size: 13px;
}

@media (max-width: 768px) {
  .hero-content h1 {
    font-size: 26px;
  }

  .search-bar {
    flex-direction: column;
    padding: 16px;
  }

  .search-item {
    width: 100%;
  }

  .search-guests {
    width: 100%;
  }

  .search-btn {
    width: 100%;
  }

  .house-grid {
    grid-template-columns: 1fr;
  }
}
</style>
