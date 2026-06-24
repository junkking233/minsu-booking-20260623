<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Search, StarFilled, House, Lock, Service, Medal, LocationFilled, Right
} from '@element-plus/icons-vue';
import { houseApi } from '@/api/houseApi';
import { configApi } from '@/api/configApi';
import HouseCard from '@/components/common/HouseCard.vue';

const router = useRouter();

interface CityItem { id: number; name: string; sort: number; }
interface HouseItem {
  id: number; name: string; city: string; price: number;
  type: string; capacity: number; coverImage: string;
  rating: number; sales: number; facilities: string[]; available: boolean;
}

const cities = ref<CityItem[]>([]);
const recommendHouses = ref<HouseItem[]>([]);
const loading = ref(false);

const searchForm = ref({ city: '', checkIn: '', checkOut: '', guests: 1 });

function onSearch() {
  const query: Record<string, string> = {};
  if (searchForm.value.city) query.city = searchForm.value.city;
  if (searchForm.value.checkIn) query.checkIn = searchForm.value.checkIn;
  if (searchForm.value.checkOut) query.checkOut = searchForm.value.checkOut;
  if (searchForm.value.guests > 1) query.guests = String(searchForm.value.guests);
  router.push({ path: '/portal/houses', query });
}

function goCity(city: string) { router.push({ path: '/portal/houses', query: { city } }); }
function goDetail(id: number | string) { router.push(`/portal/houses/${id}`); }
function goHouses() { router.push('/portal/houses'); }

async function loadData() {
  loading.value = true;
  try {
    const [citiesData, housesData] = await Promise.all([
      configApi.cities(),
      houseApi.search({ pageSize: 6, sort: 'recommend' }),
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

// 热门城市配色
const cityColors = ['#e0f2fe', '#fef3ee', '#eef5ef', '#fffbeb', '#eff6ff', '#f0f9ff'];
function cityColor(index: number) { return cityColors[index % cityColors.length]; }
</script>

<template>
  <div class="home-page" v-loading="loading">
    <!-- ========== Hero 区域 ========== -->
    <section class="hero-section">
      <!-- 背景装饰 -->
      <div class="hero-bg-orb hero-orb--blue"></div>
      <div class="hero-bg-orb hero-orb--coral"></div>
      <div class="hero-bg-dots"></div>

      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          精选品质民宿 · 2026
        </div>
        <h1>发现旅途中的<span class="hero-accent">家</span></h1>
        <p>精选品质民宿，开启美好旅程。无论是周末度假还是长期旅居，都能找到理想温暖的居所。</p>

        <!-- 搜索栏 — 分段药丸设计 -->
        <div class="search-bar">
          <div class="search-fields">
            <div class="search-field search-field--city">
              <label class="search-label">城市</label>
              <el-select
                v-model="searchForm.city"
                placeholder="选择城市"
                clearable
                size="large"
                class="search-select"
                popper-class="search-city-popper"
              >
                <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.name" />
              </el-select>
            </div>
            <div class="search-divider"></div>
            <div class="search-field">
              <label class="search-label">入住</label>
              <el-date-picker
                v-model="searchForm.checkIn"
                type="date"
                placeholder="选择日期"
                :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000"
                size="large"
                class="search-date"
              />
            </div>
            <div class="search-divider"></div>
            <div class="search-field">
              <label class="search-label">离店</label>
              <el-date-picker
                v-model="searchForm.checkOut"
                type="date"
                placeholder="选择日期"
                :disabled-date="(d: Date) => !searchForm.checkIn || d.getTime() <= new Date(searchForm.checkIn).getTime()"
                size="large"
                class="search-date"
              />
            </div>
            <div class="search-divider"></div>
            <div class="search-field">
              <label class="search-label">人数</label>
              <div class="guests-select">
                <el-input-number
                  v-model="searchForm.guests"
                  :min="1" :max="20"
                  size="large"
                  class="search-guests"
                />
              </div>
            </div>
          </div>
          <el-button class="search-btn-coral" size="large" @click="onSearch">
            <el-icon :size="18"><Search /></el-icon>
            搜索房源
          </el-button>
        </div>
      </div>
    </section>

    <!-- ========== 热门城市 ========== -->
    <section class="section" v-if="cities.length > 0">
      <div class="section-header">
        <div>
          <span class="section-label">探索目的地</span>
          <h2 class="section-title">热门城市</h2>
        </div>
        <el-button text type="primary" @click="goHouses">查看全部民宿 <el-icon><Right /></el-icon></el-button>
      </div>
      <div class="city-cards">
        <div
          v-for="(c, idx) in cities"
          :key="c.id"
          class="city-card interactive"
          :style="{ '--city-bg': cityColor(idx) }"
          @click="goCity(c.name)"
        >
          <div class="city-icon">
            <el-icon :size="22"><LocationFilled /></el-icon>
          </div>
          <span class="city-name">{{ c.name }}</span>
          <span class="city-explore">探索民宿</span>
        </div>
      </div>
    </section>

    <!-- ========== 推荐房源 ========== -->
    <section class="section">
      <div class="section-header">
        <div>
          <span class="section-label">品质精选</span>
          <h2 class="section-title">推荐房源</h2>
          <p class="section-desc">为您精选的优质民宿，享受温馨旅途</p>
        </div>
        <el-button text type="primary" @click="goHouses">
          更多房源 <el-icon><Right /></el-icon>
        </el-button>
      </div>
      <div class="house-grid" v-if="recommendHouses.length > 0">
        <HouseCard
          v-for="house in recommendHouses"
          :key="house.id"
          :house="{
            id: house.id,
            name: house.name,
            city: house.city,
            price: house.price,
            rating: house.rating,
            type: house.type,
            capacity: house.capacity,
            coverImage: house.coverImage,
            sales: house.sales,
          }"
          @click="goDetail"
        />
      </div>
      <el-empty v-else description="暂无推荐房源" />
    </section>

    <!-- ========== 特色服务 ========== -->
    <section class="section features-section">
      <div class="section-header" style="text-align: center;">
        <span class="section-label">品质承诺</span>
        <h2 class="section-title">为什么选择我们</h2>
        <p class="section-desc">我们致力于为您提供最优质的住宿体验</p>
      </div>
      <div class="features-grid">
        <div class="feature-card" v-for="(f, idx) in [
          { icon: House, title: '精选房源', desc: '严格筛选每一间房源，确保品质与舒适度，让您住得放心', color: 'blue' },
          { icon: Lock, title: '安全支付', desc: '多种支付方式，资金安全有保障，交易透明放心', color: 'coral' },
          { icon: Service, title: '贴心服务', desc: '7×24小时客服支持，随时为您解答旅途中的问题', color: 'sage' },
          { icon: Medal, title: '真实评价', desc: '真实住客评价，帮您做出最佳选择，所见即所得', color: 'amber' },
        ]" :key="f.title">
          <div class="feature-icon" :class="`feature-icon--${f.color}`">
            <el-icon :size="26"><component :is="f.icon" /></el-icon>
          </div>
          <span class="feature-number">{{ String(idx + 1).padStart(2, '0') }}</span>
          <h3>{{ f.title }}</h3>
          <p>{{ f.desc }}</p>
        </div>
      </div>
    </section>

    <!-- ========== 好评展示 ========== -->
    <section class="section testimonials-section">
      <div class="testimonial-banner">
        <div class="testimonial-content">
          <span class="section-label text-warm">住客反馈</span>
          <h2 class="section-title">旅行者的真实体验</h2>
          <p class="section-desc">超过 1,000+ 位旅行者通过我们找到了理想的民宿</p>
          <div class="testimonial-stats">
            <div class="t-stat">
              <strong>1000+</strong>
              <span>满意住客</span>
            </div>
            <div class="t-stat">
              <strong>4.8</strong>
              <span>平均评分</span>
            </div>
            <div class="t-stat">
              <strong>50+</strong>
              <span>合作城市</span>
            </div>
            <div class="t-stat">
              <strong>99%</strong>
              <span>服务好评率</span>
            </div>
          </div>
        </div>
        <div class="testimonial-cards">
          <div class="review-quote-card">
            <div class="quote-mark">"</div>
            <p>房间干净整洁，房东热情周到。窗外的山景美极了，早上醒来就能看到日出，完美的一次旅行体验！</p>
            <div class="quote-author">
              <el-avatar :size="36" style="background: linear-gradient(135deg, var(--c-primary), var(--c-primary-light)); color: #fff;">张</el-avatar>
              <div>
                <strong>张女士</strong>
                <span>入住：山景民宿 · 杭州</span>
              </div>
              <div class="quote-stars">
                <el-icon v-for="i in 5" :key="i"><StarFilled /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 1280px; margin: 0 auto; padding: 0 20px;
}

/* ==================== Hero ==================== */
.hero-section {
  position: relative;
  padding: 90px 0 70px;
  text-align: center;
  overflow: hidden;
  background:
    radial-gradient(ellipse 80% 50% at 20% 10%, rgb(2 132 199 / 7%), transparent),
    radial-gradient(ellipse 70% 40% at 80% 80%, rgb(234 115 86 / 6%), transparent),
    radial-gradient(ellipse 60% 60% at 50% 50%, rgb(2 132 199 / 3%), transparent),
    linear-gradient(175deg, #f0f6fa 0%, var(--c-bg) 60%, var(--c-bg-warm) 100%);
  border-radius: var(--radius-2xl);
  margin-bottom: 8px;
  box-shadow: var(--shadow-md);
}

/* 背景装饰球 */
.hero-bg-orb {
  position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.4;
}
.hero-orb--blue {
  width: 360px; height: 360px;
  top: -120px; right: -100px;
  background: rgb(2 132 199 / 12%);
}
.hero-orb--coral {
  width: 240px; height: 240px;
  bottom: -60px; left: -80px;
  background: rgb(234 115 86 / 10%);
}
.hero-bg-dots {
  position: absolute; inset: 0;
  background-image: radial-gradient(rgb(2 132 199 / 5%) 1px, transparent 1px);
  background-size: 32px 32px;
  opacity: 0.5;
}

.hero-content { position: relative; z-index: 1; }

.hero-badge {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 6px 20px;
  background: rgb(255 255 255 / 80%);
  backdrop-filter: blur(8px);
  color: var(--c-primary);
  font-size: 13px; font-weight: 600;
  border-radius: var(--radius-full);
  margin-bottom: 28px;
  border: 1px solid rgb(2 132 199 / 15%);
  box-shadow: var(--shadow-sm);
}
.badge-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: var(--c-coral);
}

.hero-content h1 {
  margin: 0 0 18px;
  font-size: 44px; font-weight: 800;
  letter-spacing: -1.5px; color: var(--c-ink);
  line-height: 1.2;
}
.hero-accent {
  background: linear-gradient(135deg, var(--c-coral), var(--c-coral-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-content > p {
  margin: 0 auto 40px;
  color: var(--c-muted);
  font-size: 16px; max-width: 520px; line-height: 1.7;
}

/* ---- 搜索栏 ---- */
.search-bar {
  display: flex; align-items: center; gap: 16px;
  max-width: 820px; margin: 0 auto;
  padding: 8px 8px 8px 24px;
  background: var(--c-surface);
  border-radius: var(--radius-xl);
  box-shadow:
    0 8px 32px rgb(26 35 50 / 8%),
    0 2px 4px rgb(26 35 50 / 4%);
  border: 1px solid rgb(229 232 236 / 80%);
}
.search-fields {
  flex: 1; display: flex; align-items: flex-end; gap: 0;
}
.search-field { flex: 1; min-width: 110px; padding: 0 12px; }
.search-field--city { flex: 1.3; min-width: 110px; }
.search-field:first-child { padding-left: 0; }
.search-date { min-width: 110px; width: 100%; }
.search-guests { min-width: 90px; }
.search-label {
  display: block; font-size: 12px; font-weight: 600;
  color: var(--c-muted); margin-bottom: 6px;
  letter-spacing: 0.3px; text-align: left;
}
.search-divider {
  width: 1px; height: 36px;
  background: var(--c-line);
  align-self: center;
}
.search-select {
  width: 100%;
  min-width: 100px;
}
.search-select :deep(.el-input__wrapper) {
  box-shadow: none !important; background: transparent;
  padding: 0 8px; border-radius: var(--radius-sm) !important;
  border: 1px solid transparent;
}
.search-select :deep(.el-input__wrapper:hover) {
  border-color: var(--c-line) !important;
  box-shadow: none !important;
}
.search-select :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgb(2 132 199 / 10%) !important;
  border-color: var(--c-primary-light) !important;
}

.search-date {
  width: 100%;
}
.search-date :deep(.el-input__wrapper) {
  box-shadow: none !important; background: transparent;
  padding: 0 8px; border-radius: var(--radius-sm) !important;
  border: 1px solid transparent;
}
.search-date :deep(.el-input__wrapper:hover) {
  border-color: var(--c-line) !important;
  box-shadow: none !important;
}
.search-date :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgb(2 132 199 / 10%) !important;
  border-color: var(--c-primary-light) !important;
}

.search-guests {
  width: 100%;
}
.search-guests :deep(.el-input__wrapper) {
  box-shadow: none !important; background: transparent;
  padding: 0 8px; border-radius: var(--radius-sm) !important;
  border: 1px solid transparent;
}
.search-guests :deep(.el-input__wrapper:hover) {
  border-color: var(--c-line) !important;
  box-shadow: none !important;
}
.search-guests :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgb(2 132 199 / 10%) !important;
  border-color: var(--c-primary-light) !important;
}
.guests-select { width: 100%; }

.search-btn-coral {
  flex-shrink: 0;
  height: 48px; padding: 0 28px;
  border-radius: var(--radius-xl);
  font-size: 15px; font-weight: 700;
  background: linear-gradient(135deg, var(--c-coral-light), var(--c-coral), var(--c-coral-dark));
  border: none; color: #fff;
  box-shadow: 0 4px 16px rgb(234 115 86 / 35%);
  transition: all var(--transition-base);
  display: flex; align-items: center; gap: 8px;
}
.search-btn-coral:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgb(234 115 86 / 45%);
  filter: brightness(1.05);
}
.search-btn-coral:active {
  transform: translateY(0);
}

/* ==================== Sections ==================== */
.section { margin: 56px 0; }
.section-header {
  display: flex; align-items: flex-end; justify-content: space-between;
  gap: 16px; margin-bottom: 28px;
}
.section-label {
  display: inline-block;
  font-size: 12px; font-weight: 700; letter-spacing: 0.8px;
  text-transform: uppercase; color: var(--c-primary);
  margin-bottom: 6px;
}
.section-title {
  font-size: 26px; font-weight: 800; margin: 0 0 6px; color: var(--c-ink);
  letter-spacing: -0.5px;
}
.section-desc {
  margin: 0; color: var(--c-muted); font-size: 14px;
}

/* ==================== City Cards ==================== */
.city-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}
.city-card {
  display: flex; flex-direction: column; align-items: center;
  gap: 8px; padding: 20px 16px;
  background: var(--city-bg, var(--c-primary-bg));
  border-radius: var(--radius-lg); border: 1px solid transparent;
  transition: all var(--transition-base);
}
.city-card:hover { border-color: var(--c-primary-light); transform: translateY(-3px); box-shadow: var(--shadow-md); }
.city-icon { color: var(--c-primary); opacity: 0.6; }
.city-name { font-size: 16px; font-weight: 600; color: var(--c-ink); }
.city-explore { font-size: 12px; color: var(--c-muted); }

/* ==================== House Grid ==================== */
.house-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

/* ==================== Features ==================== */
.features-section { padding: 20px 0 48px; }
.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.feature-card {
  position: relative;
  padding: 32px 24px;
  background: var(--c-surface);
  border: 1px solid rgb(229 232 236 / 70%);
  border-radius: var(--radius-lg);
  text-align: left;
  transition: all var(--transition-base);
  overflow: hidden;
}
.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--c-primary-light);
}
.feature-number {
  position: absolute; top: 16px; right: 20px;
  font-size: 40px; font-weight: 800; color: var(--c-line-light);
  line-height: 1; pointer-events: none;
}
.feature-icon {
  width: 52px; height: 52px;
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--radius-md);
  margin-bottom: 20px; color: #fff;
}
.feature-icon--blue  { background: linear-gradient(135deg, var(--c-primary), var(--c-primary-light)); }
.feature-icon--coral { background: linear-gradient(135deg, var(--c-coral), var(--c-coral-light)); }
.feature-icon--sage  { background: linear-gradient(135deg, var(--c-sage), #8fbc9a); }
.feature-icon--amber { background: linear-gradient(135deg, var(--c-amber), #fbbf24); }

.feature-card h3 {
  margin: 0 0 8px; font-size: 16px; font-weight: 700; color: var(--c-ink);
}
.feature-card p {
  margin: 0; font-size: 13px; color: var(--c-muted); line-height: 1.7;
}

/* ==================== Testimonials ==================== */
.testimonials-section { padding-bottom: 48px; }
.testimonial-banner {
  display: grid; grid-template-columns: 1fr 1fr; gap: 40px; align-items: center;
  padding: 48px 40px;
  background: linear-gradient(135deg, var(--c-bg-warm), var(--c-surface));
  border-radius: var(--radius-xl);
  border: 1px solid var(--c-sand-dark);
}
.testimonial-stats {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px;
  margin-top: 28px;
}
.t-stat {
  text-align: center; padding: 16px 12px;
  background: var(--c-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--c-line);
}
.t-stat strong {
  display: block; font-size: 22px; font-weight: 800;
  color: var(--c-primary); letter-spacing: -0.5px;
}
.t-stat span {
  font-size: 12px; color: var(--c-muted); margin-top: 4px;
  display: block;
}

.review-quote-card {
  position: relative;
  padding: 32px 28px;
  background: var(--c-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}
.quote-mark {
  font-size: 72px; font-family: Georgia, serif;
  color: var(--c-coral); line-height: 0.5;
  opacity: 0.3; position: absolute; top: 20px; left: 20px;
}
.review-quote-card p {
  position: relative; z-index: 1;
  font-size: 15px; line-height: 1.8; color: var(--c-body);
  margin: 0 0 20px;
}
.quote-author { display: flex; align-items: center; gap: 12px; }
.quote-author strong { font-size: 14px; color: var(--c-ink); display: block; }
.quote-author span { font-size: 12px; color: var(--c-muted); }
.quote-stars {
  margin-left: auto; display: flex; gap: 2px;
  color: var(--c-amber); font-size: 14px;
}

/* ==================== Responsive ==================== */
@media (max-width: 1024px) {
  .features-grid { grid-template-columns: repeat(2, 1fr); }
  .testimonial-banner { grid-template-columns: 1fr; }
  .testimonial-stats { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .hero-section { padding: 60px 16px 40px; }
  .hero-content h1 { font-size: 30px; }
  .hero-content > p { font-size: 14px; }
  .search-bar {
    flex-direction: column; padding: 16px;
  }
  .search-fields { flex-direction: column; }
  .search-field { padding: 8px 0; }
  .search-divider {
    width: 100%; height: 1px;
  }
  .search-btn-coral { width: 100%; justify-content: center; }
  .house-grid { grid-template-columns: 1fr; }
  .features-grid { grid-template-columns: 1fr; }
  .city-cards { grid-template-columns: repeat(2, 1fr); }
}
</style>

<style>
/* 非 scoped: 下拉面板宽度适配 */
.search-city-popper {
  min-width: 160px !important;
}
.search-city-popper .el-select-dropdown__item {
  padding: 8px 16px;
  font-size: 14px;
}
</style>
