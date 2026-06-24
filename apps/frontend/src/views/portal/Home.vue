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
      <div class="hero-bg">
        <div class="hero-orb hero-orb-1" />
        <div class="hero-orb hero-orb-2" />
      </div>
      <div class="hero-content">
        <div class="hero-badge">精选品质民宿</div>
        <h1>发现旅途中的家</h1>
        <p>精选品质民宿，开启美好旅程。无论是周末度假还是长期旅居，都能找到理想的居所。</p>
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
      <div class="section-header">
        <h2 class="section-title">热门城市</h2>
        <p class="section-desc">探索热门旅行目的地</p>
      </div>
      <div class="city-tags">
        <div
          v-for="c in cities"
          :key="c.id"
          class="city-tag"
          @click="goCity(c.name)"
        >
          <span class="city-name">{{ c.name }}</span>
        </div>
      </div>
    </section>

    <!-- 推荐房源 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">推荐房源</h2>
        <p class="section-desc">为您精选的优质民宿</p>
      </div>
      <div class="house-grid" v-if="recommendHouses.length > 0">
        <div
          v-for="house in recommendHouses"
          :key="house.id"
          class="house-card"
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
        </div>
      </div>
      <el-empty v-else description="暂无推荐房源" />
    </section>

    <!-- 特色服务 -->
    <section class="section features-section">
      <div class="section-header">
        <h2 class="section-title">为什么选择我们</h2>
        <p class="section-desc">我们致力于为您提供最优质的住宿体验</p>
      </div>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon-wrap">
            <span>🏠</span>
          </div>
          <h3>精选房源</h3>
          <p>严格筛选每一间房源，确保品质与舒适度</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon-wrap">
            <span>🔒</span>
          </div>
          <h3>安全支付</h3>
          <p>多种支付方式，资金安全有保障</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon-wrap">
            <span>💬</span>
          </div>
          <h3>贴心服务</h3>
          <p>7x24小时客服支持，随时为您解答</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon-wrap">
            <span>⭐</span>
          </div>
          <h3>真实评价</h3>
          <p>真实住客评价，帮您做出最佳选择</p>
        </div>
      </div>
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
  position: relative;
  padding: 80px 0 60px;
  text-align: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}

.hero-orb-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: 10%;
  background: rgb(14 165 233 / 8%);
}

.hero-orb-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  right: 15%;
  background: rgb(37 99 235 / 6%);
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-block;
  padding: 6px 16px;
  background: var(--c-primary-bg);
  color: var(--c-primary);
  font-size: 13px;
  font-weight: 600;
  border-radius: 20px;
  margin-bottom: 20px;
  border: 1px solid rgb(14 165 233 / 15%);
}

.hero-content h1 {
  margin: 0 0 16px;
  font-size: 42px;
  font-weight: 800;
  letter-spacing: -1.5px;
  color: var(--c-ink);
  background: linear-gradient(135deg, var(--c-ink) 0%, var(--c-primary-dark) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-content > p {
  margin: 0 0 36px;
  color: var(--c-muted);
  font-size: 17px;
  max-width: 560px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 880px;
  margin: 0 auto;
  padding: 14px;
  background: var(--c-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  flex-wrap: wrap;
  justify-content: center;
  border: 1px solid var(--c-line);
}

.search-item {
  width: 150px;
}

.search-guests {
  width: 100px;
}

.search-btn {
  height: 42px;
  border-radius: var(--radius-md);
  font-weight: 600;
  padding: 0 24px;
}

/* Sections */
.section {
  margin: 48px 0;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 800;
  margin: 0 0 6px;
  color: var(--c-ink);
}

.section-desc {
  margin: 0;
  color: var(--c-muted);
  font-size: 14px;
}

/* City tags */
.city-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.city-tag {
  cursor: pointer;
  padding: 12px 24px;
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
}

.city-tag:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--c-primary-light);
  color: var(--c-primary);
}

.city-name {
  font-size: 15px;
  font-weight: 500;
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
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
}

.house-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}

.house-card:hover .card-cover img {
  transform: scale(1.05);
}

.card-cover {
  position: relative;
  height: 220px;
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
  top: 12px;
  left: 12px;
  padding: 5px 14px;
  background: rgb(0 0 0 / 55%);
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  backdrop-filter: blur(4px);
}

.card-body {
  padding: 16px 18px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-ink);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-city {
  font-size: 13px;
  color: var(--c-muted);
  margin-bottom: 12px;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
  font-size: 13px;
}

.card-rating {
  color: var(--c-amber);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-capacity {
  color: var(--c-muted);
}

.card-bottom {
  padding-top: 14px;
  border-top: 1px solid var(--c-line);
}

.card-price strong {
  font-size: 22px;
  color: var(--c-red);
  font-weight: 700;
}

.card-price {
  color: var(--c-muted);
  font-size: 13px;
}

/* Features */
.features-section {
  padding: 48px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.feature-card {
  padding: 28px 20px;
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-lg);
  text-align: center;
  transition: all var(--transition-base);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--c-primary-light);
}

.feature-icon-wrap {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--c-primary-bg);
  border-radius: var(--radius-lg);
  font-size: 28px;
}

.feature-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--c-ink);
}

.feature-card p {
  margin: 0;
  font-size: 13px;
  color: var(--c-muted);
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-content h1 {
    font-size: 30px;
  }

  .hero-content > p {
    font-size: 15px;
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

  .features-grid {
    grid-template-columns: 1fr;
  }

  .city-tags {
    gap: 8px;
  }

  .city-tag {
    padding: 10px 18px;
  }
}
</style>
