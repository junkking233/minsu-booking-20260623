<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  StarFilled, LocationFilled, UserFilled, Select, Timer, User, Clock
} from '@element-plus/icons-vue';
import { houseApi } from '@/api/houseApi';
import { favoriteApi } from '@/api/favoriteApi';
import { reviewApi } from '@/api/reviewApi';
import { getCurrentUser } from '@/utils/auth';

const route = useRoute();
const router = useRouter();
const currentUser = getCurrentUser();
const houseId = Number(route.params.id);

interface ReviewItem {
  id: number; userName: string; userAvatar: string | null;
  rating: number; content: string; images: string[];
  reply: string; createTime: string;
}
interface HouseDetail {
  id: number; name: string; city: string; price: number;
  cleanFee: number; type: string; rooms: string; area: string;
  capacity: number; address: string; host: string;
  coverImage: string; facilities: string[]; description: string;
  rule: string; rating: number; sales: number; available: boolean;
  favorited: boolean; reviews: ReviewItem[];
}

const house = ref<HouseDetail | null>(null);
const loading = ref(true);
const favorited = ref(false);
const reviews = ref<ReviewItem[]>([]);

const checkIn = ref((route.query.checkIn as string) || '');
const checkOut = ref((route.query.checkOut as string) || '');
const guests = ref(Number(route.query.guests) || 1);

const nights = computed(() => {
  if (!checkIn.value || !checkOut.value) return 0;
  return Math.max(0, Math.ceil((new Date(checkOut.value).getTime() - new Date(checkIn.value).getTime()) / 86400000));
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
    try { reviews.value = (await reviewApi.listByHouse(houseId)) as ReviewItem[]; }
    catch { reviews.value = []; }
  } catch (e) {
    ElMessage.error('加载房源详情失败');
  } finally { loading.value = false; }
}

function goBooking() {
  if (!currentUser) { ElMessage.warning('请先登录'); router.push('/login'); return; }
  const query: Record<string, string> = {};
  if (checkIn.value) query.checkIn = checkIn.value;
  if (checkOut.value) query.checkOut = checkOut.value;
  if (guests.value > 1) query.guests = String(guests.value);
  router.push({ path: `/portal/booking/${houseId}`, query });
}

async function toggleFavorite() {
  if (!currentUser) { ElMessage.warning('请先登录'); return; }
  try {
    if (favorited.value) { await favoriteApi.remove(houseId); favorited.value = false; ElMessage.success('已取消收藏'); }
    else { await favoriteApi.add(houseId); favorited.value = true; ElMessage.success('收藏成功'); }
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '操作失败'); }
}

onMounted(loadDetail);
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <div class="page-inner" v-if="house">
      <!-- ====== 图片画廊 ====== -->
      <div class="gallery">
        <div class="gallery-main">
          <img :src="house.coverImage || '/placeholder.svg'" :alt="house.name" />
        </div>
        <div class="gallery-side">
          <div class="gallery-thumb">
            <img :src="house.coverImage || '/placeholder.svg'" alt="" />
          </div>
          <div class="gallery-thumb">
            <img :src="house.coverImage || '/placeholder.svg'" alt="" />
          </div>
        </div>
        <div class="gallery-fav" :class="{ favorited }" @click="toggleFavorite">
          <el-icon :size="20"><component :is="favorited ? StarFilled : StarFilled" /></el-icon>
        </div>
      </div>

      <!-- ====== 两列布局 ====== -->
      <div class="detail-grid">
        <!-- 左侧 -->
        <div class="detail-main">
          <!-- 标题 & 评分 -->
          <div class="title-block">
            <div class="title-row">
              <h1 class="house-name">{{ house.name }}</h1>
              <div class="rating-pill" v-if="house.rating">
                <el-icon><StarFilled /></el-icon>
                <strong>{{ house.rating }}</strong>
                <span v-if="house.sales">({{ house.sales }} 条评价)</span>
              </div>
            </div>
            <div class="meta-row">
              <span class="meta-item"><el-icon><LocationFilled /></el-icon>{{ house.city }}</span>
              <span class="meta-sep">·</span>
              <span class="meta-item">{{ house.type }}</span>
              <span class="meta-sep">·</span>
              <span class="meta-item">{{ house.rooms }}间卧室</span>
              <span class="meta-sep">·</span>
              <span class="meta-item">{{ house.area }}</span>
              <span class="meta-sep">·</span>
              <span class="meta-item"><el-icon><User /></el-icon>宜住{{ house.capacity }}人</span>
            </div>
          </div>

          <div class="divider-soft"></div>

          <!-- 房东 -->
          <div class="info-block">
            <h3><el-icon><UserFilled /></el-icon>房东信息</h3>
            <div class="host-card">
              <el-avatar :size="48" style="background: linear-gradient(135deg, var(--c-primary), var(--c-primary-light)); color: #fff;">{{ house.host?.charAt(0) || '房' }}</el-avatar>
              <div class="host-info">
                <strong>{{ house.host }}</strong>
                <span class="host-detail"><el-icon><LocationFilled /></el-icon>{{ house.address }}</span>
              </div>
            </div>
          </div>

          <!-- 设施 -->
          <div class="info-block" v-if="house.facilities?.length">
            <h3><el-icon><Select /></el-icon>配套设施</h3>
            <div class="facilities-grid">
              <div v-for="f in house.facilities" :key="f" class="facility-item">
                <span class="facility-dot"></span>{{ f }}
              </div>
            </div>
          </div>

          <!-- 介绍 -->
          <div class="info-block" v-if="house.description">
            <h3>房源介绍</h3>
            <p class="description-text">{{ house.description }}</p>
          </div>

          <!-- 规则 -->
          <div class="info-block" v-if="house.rule">
            <h3><el-icon><Timer /></el-icon>入住须知</h3>
            <div class="rule-card">
              <p>{{ house.rule }}</p>
            </div>
          </div>

          <!-- 评价 -->
          <div class="info-block" v-if="reviews.length">
            <h3>住客评价 <span class="review-count">({{ reviews.length }})</span></h3>
            <div class="review-list">
              <div v-for="r in reviews" :key="r.id" class="review-card">
                <div class="review-top">
                  <div class="review-user-info">
                    <el-avatar :size="36" style="background: linear-gradient(135deg, var(--c-line-light), var(--c-muted-light)); color: var(--c-ink-light);">{{ r.userName?.charAt(0) }}</el-avatar>
                    <div>
                      <strong>{{ r.userName }}</strong>
                      <span class="review-date"><el-icon><Clock /></el-icon>{{ r.createTime?.slice(0, 10) }}</span>
                    </div>
                  </div>
                  <div class="review-stars">
                    <el-icon v-for="i in 5" :key="i" :class="{ filled: i <= r.rating }"><StarFilled /></el-icon>
                  </div>
                </div>
                <p class="review-content">{{ r.content }}</p>
                <div v-if="r.images?.length" class="review-images">
                  <img v-for="(img, i) in r.images" :key="i" :src="img" class="review-thumb" />
                </div>
                <div v-if="r.reply" class="review-reply">
                  <el-icon><UserFilled /></el-icon><strong>房东回复：</strong>{{ r.reply }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧预订卡 -->
        <div class="detail-sidebar">
          <div class="booking-card card-premium">
            <div class="booking-header">
              <div class="booking-price">
                <strong>&yen;{{ house.price }}</strong><span>/晚</span>
              </div>
              <div class="booking-rating" v-if="house.rating">
                <el-icon><StarFilled /></el-icon>
                <strong>{{ house.rating }}</strong>
                <span v-if="house.sales">· {{ house.sales }}条评价</span>
              </div>
            </div>

            <div class="booking-form">
              <div class="date-group">
                <div class="date-field">
                  <label>入住</label>
                  <el-date-picker v-model="checkIn" type="date" placeholder="选择日期" class="date-input" :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000" />
                </div>
                <span class="date-arrow">→</span>
                <div class="date-field">
                  <label>离店</label>
                  <el-date-picker v-model="checkOut" type="date" placeholder="选择日期" class="date-input" :disabled-date="(d: Date) => !checkIn || d.getTime() <= new Date(checkIn).getTime()" />
                </div>
              </div>
              <div class="guests-field">
                <label>入住人数</label>
                <el-input-number v-model="guests" :min="1" :max="house.capacity" class="guests-input" />
              </div>
            </div>

            <div class="booking-summary" v-if="nights > 0">
              <div class="summary-row">
                <span>&yen;{{ house.price }} × {{ nights }} 晚房费</span>
                <span>&yen;{{ house.price * nights }}</span>
              </div>
              <div class="summary-row">
                <span>清洁费</span>
                <span>&yen;{{ house.cleanFee }}</span>
              </div>
              <div class="summary-row summary-total">
                <span>合计</span>
                <strong class="text-price">&yen;{{ totalPrice }}</strong>
              </div>
            </div>

            <el-button class="booking-btn-coral" size="large" @click="goBooking">
              <el-icon><Select /></el-icon> 立即预约
            </el-button>

            <el-button
              class="fav-btn-outline"
              size="large"
              @click="toggleFavorite"
            >
              {{ favorited ? '♥ 已收藏' : '♡ 收藏房源' }}
            </el-button>

            <div class="trust-strip">
              <div class="trust-item"><span class="trust-check">✓</span>免费取消</div>
              <div class="trust-item"><span class="trust-check">✓</span>安全支付</div>
              <div class="trust-item"><span class="trust-check">✓</span>预订即确认</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page { max-width: 1200px; margin: 0 auto; padding: 24px 20px; }

/* ---- Gallery ---- */
.gallery {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 8px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: 28px;
  position: relative;
  height: 400px;
}
.gallery-main {
  grid-row: span 2; overflow: hidden;
}
.gallery-main img,
.gallery-thumb img {
  width: 100%; height: 100%; object-fit: cover;
  transition: transform var(--transition-slow);
}
.gallery:hover .gallery-main img { transform: scale(1.02); }
.gallery-side {
  display: flex; flex-direction: column; gap: 8px;
}
.gallery-thumb {
  flex: 1; overflow: hidden;
}

.gallery-fav {
  position: absolute; top: 16px; right: 16px;
  width: 44px; height: 44px;
  display: flex; align-items: center; justify-content: center;
  background: rgb(255 255 255 / 90%);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  cursor: pointer;
  font-size: 20px; color: var(--c-muted-light);
  transition: all var(--transition-bounce);
  box-shadow: var(--shadow-md);
  z-index: 5;
}
.gallery-fav.favorited { color: var(--c-coral); background: var(--c-coral-bg); }
.gallery-fav:hover { transform: scale(1.12); color: var(--c-coral); }

/* ---- Detail Grid ---- */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 32px; align-items: start;
}

.title-block { margin-bottom: 8px; }
.title-row {
  display: flex; align-items: center; justify-content: space-between;
  gap: 16px; flex-wrap: wrap; margin-bottom: 10px;
}
.house-name { margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -0.5px; color: var(--c-ink); }
.rating-pill {
  display: flex; align-items: center; gap: 5px;
  padding: 6px 14px;
  background: var(--c-amber-bg);
  border-radius: var(--radius-full);
  white-space: nowrap;
  flex-shrink: 0;
}
.rating-pill .el-icon { color: var(--c-amber); }
.rating-pill strong { font-size: 15px; color: var(--c-amber); }
.rating-pill span { font-size: 12px; color: var(--c-muted); }

.meta-row {
  display: flex; flex-wrap: wrap; align-items: center; gap: 4px;
  font-size: 14px; color: var(--c-muted);
}
.meta-item { display: flex; align-items: center; gap: 4px; }
.meta-sep { color: var(--c-muted-light); }

.divider-soft {
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--c-line), transparent);
  margin: 20px 0;
}

/* ---- Info Blocks ---- */
.info-block { margin-bottom: 32px; }
.info-block h3 {
  display: flex; align-items: center; gap: 8px;
  margin: 0 0 14px;
  font-size: 17px; font-weight: 700; color: var(--c-ink);
}
.info-block h3 .el-icon { color: var(--c-primary); }

.host-card {
  display: flex; align-items: center; gap: 14px;
  padding: 16px;
  background: var(--c-bg); border-radius: var(--radius-md);
}
.host-info strong { display: block; font-size: 15px; color: var(--c-ink); margin-bottom: 4px; }
.host-detail { display: flex; align-items: center; gap: 4px; font-size: 13px; color: var(--c-muted); }

.facilities-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
}
.facility-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px;
  background: var(--c-bg); border-radius: var(--radius-md);
  font-size: 14px; color: var(--c-body);
}
.facility-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--c-primary);
  opacity: 0.6; flex-shrink: 0;
}

.description-text {
  font-size: 15px; line-height: 1.8; color: var(--c-body);
  white-space: pre-wrap; margin: 0;
}
.rule-card {
  padding: 16px 20px;
  background: var(--c-bg-warm);
  border: 1px solid var(--c-sand-dark);
  border-radius: var(--radius-md);
  font-size: 14px; color: var(--c-body); line-height: 1.7;
}
.rule-card p { margin: 0; white-space: pre-wrap; }

/* ---- Reviews ---- */
.review-count { font-weight: 400; color: var(--c-muted); font-size: 14px; }
.review-card {
  padding: 20px 0;
  border-bottom: 1px solid var(--c-line-light);
}
.review-card:last-child { border-bottom: none; }

.review-top {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 12px; margin-bottom: 10px;
}
.review-user-info { display: flex; align-items: center; gap: 10px; }
.review-user-info strong { font-size: 14px; color: var(--c-ink); display: block; }
.review-date { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--c-muted); }

.review-stars { display: flex; gap: 2px; }
.review-stars .el-icon { font-size: 14px; color: var(--c-muted-light); }
.review-stars .el-icon.filled { color: var(--c-amber); }

.review-content { margin: 0 0 8px; font-size: 14px; line-height: 1.7; color: var(--c-body); }

.review-images { display: flex; gap: 8px; margin-bottom: 8px; }
.review-thumb {
  width: 80px; height: 80px;
  object-fit: cover; border-radius: var(--radius-sm);
}

.review-reply {
  display: flex; align-items: flex-start; gap: 6px;
  padding: 12px 16px;
  background: var(--c-bg); border-radius: var(--radius-md);
  font-size: 13px; color: var(--c-body); line-height: 1.6;
}
.review-reply .el-icon { color: var(--c-primary); margin-top: 2px; }

/* ---- Booking Sidebar (Premium) ---- */
.booking-card { padding: 28px; border: none; }
.booking-header {
  display: flex; align-items: baseline; justify-content: space-between;
  gap: 12px; margin-bottom: 20px;
}
.booking-price strong {
  font-size: 28px; color: var(--c-price); font-weight: 800; letter-spacing: -0.5px;
}
.booking-price span { color: var(--c-muted); font-size: 14px; }
.booking-rating {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px;
}
.booking-rating .el-icon { color: var(--c-amber); }
.booking-rating strong { color: var(--c-ink); }
.booking-rating span { color: var(--c-muted); }

.booking-form { margin-bottom: 18px; display: flex; flex-direction: column; gap: 12px; }
.booking-form label {
  display: block; font-size: 12px; font-weight: 600;
  color: var(--c-muted); margin-bottom: 5px; letter-spacing: 0.2px;
}
.date-group { display: flex; align-items: center; gap: 6px; }
.date-field { flex: 1; min-width: 100px; }
.date-input { width: 100%; min-width: 100px; }
.date-arrow { color: var(--c-muted-light); font-size: 14px; padding-top: 20px; flex-shrink: 0; }
.guests-input { width: 100%; }

.booking-summary { margin-bottom: 20px; }
.summary-row {
  display: flex; justify-content: space-between;
  padding: 7px 0; font-size: 14px; color: var(--c-body);
}
.summary-total {
  border-top: 1px solid var(--c-line); margin-top: 6px;
  padding-top: 14px; font-size: 16px; font-weight: 600;
}
.summary-total strong { font-size: 22px; }

.booking-btn-coral {
  width: 100%; height: 50px;
  background: linear-gradient(135deg, var(--c-coral-light), var(--c-coral), var(--c-coral-dark));
  border: none; color: #fff;
  font-size: 16px; font-weight: 700;
  border-radius: var(--radius-md);
  box-shadow: 0 4px 16px rgb(234 115 86 / 35%);
  margin-bottom: 10px;
  transition: all var(--transition-base);
  display: flex; align-items: center; justify-content: center; gap: 8px;
}
.booking-btn-coral:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgb(234 115 86 / 45%);
  filter: brightness(1.05);
}
.booking-btn-coral:active { transform: translateY(0); }

.fav-btn-outline {
  width: 100%; height: 44px;
  border-radius: var(--radius-md);
  border: 1.5px solid var(--c-line);
  color: var(--c-muted);
  font-weight: 500;
  transition: all var(--transition-base);
}
.fav-btn-outline:hover { border-color: var(--c-coral); color: var(--c-coral); }

.trust-strip {
  display: flex; justify-content: center; gap: 16px;
  margin-top: 16px; padding-top: 14px;
  border-top: 1px solid var(--c-line-light);
}
.trust-item { font-size: 12px; color: var(--c-muted); display: flex; align-items: center; gap: 3px; }
.trust-check { color: var(--c-sage); font-weight: 700; }

@media (max-width: 768px) {
  .detail-grid { grid-template-columns: 1fr; }
  .gallery { height: 240px; grid-template-columns: 1fr; }
  .gallery-side { display: none; }
  .booking-card { position: static; }
  .facilities-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
