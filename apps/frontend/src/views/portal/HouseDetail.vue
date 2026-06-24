<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Star } from '@element-plus/icons-vue';
import { houseApi } from '@/api/houseApi';
import { favoriteApi } from '@/api/favoriteApi';
import { getCurrentUser } from '@/utils/auth';

const route = useRoute();
const router = useRouter();
const currentUser = getCurrentUser();
const houseId = Number(route.params.id);

interface ReviewItem {
  id: number;
  userName: string;
  userAvatar: string | null;
  rating: number;
  content: string;
  images: string[];
  reply: string;
  createTime: string;
}

interface HouseDetail {
  id: number;
  name: string;
  city: string;
  price: number;
  cleanFee: number;
  type: string;
  rooms: string;
  area: string;
  capacity: number;
  address: string;
  host: string;
  coverImage: string;
  facilities: string[];
  description: string;
  rule: string;
  rating: number;
  sales: number;
  available: boolean;
  favorited: boolean;
  reviews: ReviewItem[];
}

const house = ref<HouseDetail | null>(null);
const loading = ref(true);
const favorited = ref(false);

const checkIn = ref((route.query.checkIn as string) || '');
const checkOut = ref((route.query.checkOut as string) || '');
const guests = ref(Number(route.query.guests) || 1);

const nights = computed(() => {
  if (!checkIn.value || !checkOut.value) return 0;
  const d1 = new Date(checkIn.value);
  const d2 = new Date(checkOut.value);
  return Math.max(0, Math.ceil((d2.getTime() - d1.getTime()) / 86400000));
});

const totalPrice = computed(() => {
  if (!house.value) return 0;
  return nights.value * house.value.price + (nights.value > 0 ? house.value.cleanFee : 0);
});

async function loadDetail() {
  loading.value = true;
  try {
    const params: Record<string, string> = {};
    if (checkIn.value) params.checkIn = checkIn.value;
    if (checkOut.value) params.checkOut = checkOut.value;
    const data = await houseApi.getDetail(houseId, params) as HouseDetail;
    house.value = data;
    favorited.value = data.favorited || false;
  } catch (e) {
    ElMessage.error('加载房源详情失败');
  } finally {
    loading.value = false;
  }
}

function goBooking() {
  if (!currentUser) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  const query: Record<string, string> = {};
  if (checkIn.value) query.checkIn = checkIn.value;
  if (checkOut.value) query.checkOut = checkOut.value;
  if (guests.value > 1) query.guests = String(guests.value);
  router.push({ path: `/portal/booking/${houseId}`, query });
}

async function toggleFavorite() {
  if (!currentUser) {
    ElMessage.warning('请先登录');
    return;
  }
  try {
    if (favorited.value) {
      await favoriteApi.remove(houseId);
      favorited.value = false;
      ElMessage.success('已取消收藏');
    } else {
      await favoriteApi.add(houseId);
      favorited.value = true;
      ElMessage.success('收藏成功');
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

onMounted(loadDetail);
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <div class="page-inner" v-if="house">
      <!-- 封面图 -->
      <div class="cover-section">
        <img
          :src="house.coverImage || '/placeholder.svg'"
          :alt="house.name"
          class="cover-img"
        />
      </div>

      <div class="detail-grid">
        <!-- 左侧信息 -->
        <div class="detail-main">
          <h1 class="house-name">{{ house.name }}</h1>
          <div class="house-meta">
            <span class="house-rating"><el-icon><Star /></el-icon>{{ house.rating }} · {{ house.sales }} 人预订</span>
            <span class="house-city">{{ house.city }}</span>
            <span>{{ house.type }} · {{ house.rooms }} · {{ house.area }}</span>
            <span>可住 {{ house.capacity }} 人</span>
          </div>

          <!-- 房东信息 -->
          <div class="info-block">
            <h3>房东信息</h3>
            <p>房东：{{ house.host }}</p>
            <p>地址：{{ house.address }}</p>
          </div>

          <!-- 配套设施 -->
          <div class="info-block" v-if="house.facilities?.length">
            <h3>配套设施</h3>
            <div class="facility-tags">
              <el-tag v-for="f in house.facilities" :key="f" type="info" size="large" class="facility-tag">
                {{ f }}
              </el-tag>
            </div>
          </div>

          <!-- 房源介绍 -->
          <div class="info-block" v-if="house.description">
            <h3>房源介绍</h3>
            <p class="description">{{ house.description }}</p>
          </div>

          <!-- 入住须知 -->
          <div class="info-block" v-if="house.rule">
            <h3>入住须知</h3>
            <p class="rule">{{ house.rule }}</p>
          </div>

          <!-- 评价 -->
          <div class="info-block" v-if="house.reviews?.length">
            <h3>住客评价 ({{ house.reviews.length }})</h3>
            <div class="review-list">
              <div v-for="r in house.reviews" :key="r.id" class="review-item">
                <div class="review-header">
                  <span class="review-user">{{ r.userName }}</span>
                  <span class="review-rating"><el-icon><Star /></el-icon>{{ r.rating }}</span>
                  <span class="review-time">{{ r.createTime?.slice(0, 10) }}</span>
                </div>
                <p class="review-content">{{ r.content }}</p>
                <div v-if="r.images?.length" class="review-images">
                  <img v-for="(img, i) in r.images" :key="i" :src="img" class="review-img" />
                </div>
                <div v-if="r.reply" class="review-reply">
                  <strong>房东回复：</strong>{{ r.reply }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧预约卡片 -->
        <div class="detail-sidebar">
          <div class="booking-card">
            <div class="booking-price">
              <strong>&yen;{{ house.price }}</strong><span>/晚</span>
            </div>

            <div class="booking-form">
              <el-date-picker
                v-model="checkIn"
                type="date"
                placeholder="入住日期"
                class="booking-date"
              />
              <el-date-picker
                v-model="checkOut"
                type="date"
                placeholder="离店日期"
                class="booking-date"
              />
              <el-input-number v-model="guests" :min="1" :max="house.capacity" placeholder="人数" class="booking-guests" />
            </div>

            <div class="booking-summary" v-if="nights > 0">
              <div class="summary-row">
                <span>&yen;{{ house.price }} x {{ nights }} 晚</span>
                <span>&yen;{{ house.price * nights }}</span>
              </div>
              <div class="summary-row">
                <span>清洁费</span>
                <span>&yen;{{ house.cleanFee }}</span>
              </div>
              <div class="summary-row summary-total">
                <span>合计</span>
                <strong>&yen;{{ totalPrice }}</strong>
              </div>
            </div>

            <el-button
              type="primary"
              size="large"
              class="booking-btn"
              @click="goBooking"
            >
              立即预约
            </el-button>

            <el-button
              :type="favorited ? 'warning' : 'default'"
              size="large"
              class="fav-btn"
              @click="toggleFavorite"
            >
              {{ favorited ? '♥ 已收藏' : '♡ 收藏' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.cover-section {
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 28px;
  position: relative;
}

.cover-section::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120px;
  background: linear-gradient(to top, rgb(0 0 0 / 30%), transparent);
  pointer-events: none;
}

.cover-img {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 28px;
  align-items: start;
}

.house-name {
  margin: 0 0 12px;
  font-size: 26px;
  font-weight: 800;
}

.house-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: var(--c-muted);
  font-size: 14px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--c-line);
}

.house-rating {
  color: var(--c-amber);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-block {
  margin-bottom: 28px;
}

.info-block h3 {
  margin: 0 0 12px;
  font-size: 17px;
  font-weight: 700;
}

.info-block p {
  margin: 4px 0;
  color: var(--c-body);
  font-size: 14px;
  line-height: 1.7;
}

.facility-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.facility-tag {
  border-radius: var(--radius-md);
}

.description {
  white-space: pre-wrap;
}

.rule {
  white-space: pre-wrap;
}

/* Reviews */
.review-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--c-line-light);
}

.review-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.review-user {
  font-weight: 600;
}

.review-rating {
  color: var(--c-amber);
  display: flex;
  align-items: center;
  gap: 3px;
}

.review-time {
  color: var(--c-muted);
  margin-left: auto;
}

.review-content {
  margin: 0 0 8px;
  font-size: 14px;
  line-height: 1.6;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.review-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.review-reply {
  padding: 10px 14px;
  background: var(--c-line-light);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--c-body);
}

/* Sidebar booking card */
.booking-card {
  position: sticky;
  top: 80px;
  padding: 28px;
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
}

.booking-price {
  margin-bottom: 18px;
}

.booking-price strong {
  font-size: 26px;
  color: var(--c-red);
  font-weight: 700;
}

.booking-price span {
  color: var(--c-muted);
  font-size: 14px;
}

.booking-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 18px;
}

.booking-date, .booking-guests {
  width: 100%;
}

.booking-summary {
  margin-bottom: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: var(--c-body);
}

.summary-total {
  border-top: 1px solid var(--c-line);
  margin-top: 4px;
  padding-top: 12px;
  font-size: 16px;
}

.summary-total strong {
  color: var(--c-red);
  font-size: 20px;
}

.booking-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  border-radius: var(--radius-md);
  margin-bottom: 12px;
}

.fav-btn {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-md);
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .cover-img {
    height: 240px;
  }

  .booking-card {
    position: static;
  }
}
</style>
