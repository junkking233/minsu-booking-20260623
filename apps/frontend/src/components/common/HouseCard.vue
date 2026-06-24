<script setup lang="ts">
/**
 * HouseCard — 通用房源卡片
 * 用于首页推荐、房源列表、收藏页面
 */
import { computed } from 'vue';
import { StarFilled, Star } from '@element-plus/icons-vue';

export interface HouseInfo {
  id: number | string;
  name: string;
  city: string;
  price: number;
  rating?: number;
  type?: string;
  capacity?: number;
  coverImage?: string;
  sales?: number;
  status?: string;
  favorited?: boolean;
}

const props = withDefaults(defineProps<{
  house: HouseInfo;
  favorited?: boolean;
  size?: 'default' | 'small';
  showActions?: boolean;
  loading?: boolean;
}>(), {
  favorited: false,
  size: 'default',
  showActions: true,
  loading: false,
});

const emit = defineEmits<{
  click: [id: number | string];
  book: [house: HouseInfo];
  toggleFavorite: [house: HouseInfo, current: boolean];
}>();

const coverUrl = computed(() => {
  return props.house.coverImage || '/placeholder.svg';
});

const coverHeight = computed(() => props.size === 'small' ? '180px' : '220px');
const cardPadding = computed(() => props.size === 'small' ? '14px' : '18px');

const displayRating = computed(() => {
  const r = props.house.rating;
  if (r == null || r === 0) return null;
  return Number(r).toFixed(1);
});

const displayPrice = computed(() => Number(props.house.price).toFixed(0));

function handleClick() { emit('click', props.house.id); }
function handleBook(e: Event) { e.stopPropagation(); emit('book', props.house); }
function handleFavorite(e: Event) { e.stopPropagation(); emit('toggleFavorite', props.house, props.favorited); }
</script>

<template>
  <div
    class="house-card"
    :class="[`house-card--${size}`, { 'is-loading': loading }]"
    @click="handleClick"
  >
    <!-- 封面图 -->
    <div class="house-cover" :style="{ height: coverHeight }">
      <img :src="coverUrl" :alt="house.name" loading="lazy" />

      <!-- 类型标签 -->
      <span v-if="house.type" class="cover-badge type-badge">{{ house.type }}</span>

      <!-- 不可订覆盖 -->
      <div v-if="house.status === 'UNAVAILABLE'" class="cover-unavailable">
        <span>暂不可订</span>
      </div>

      <!-- 收藏按钮 -->
      <div
        v-if="showActions"
        class="favorite-btn"
        :class="{ favorited }"
        @click="handleFavorite"
        :title="favorited ? '取消收藏' : '收藏'"
      >
        <el-icon><component :is="favorited ? StarFilled : Star" /></el-icon>
      </div>
    </div>

    <!-- 卡片内容 -->
    <div class="house-body" :style="{ padding: cardPadding }">
      <div class="house-meta">
        <span v-if="house.city" class="house-city">
          <svg width="12" height="12" viewBox="0 0 12 12"><circle cx="6" cy="5" r="2" stroke="currentColor" stroke-width="1.2" fill="none"/><path d="M4 10c0-2 2-4 2-4s2 2 2 4" stroke="currentColor" stroke-width="1.2" fill="none"/></svg>
          {{ house.city }}
        </span>
        <span v-if="displayRating" class="house-rating">
          <el-icon><StarFilled /></el-icon>
          {{ displayRating }}
          <span v-if="house.sales" class="sales-count">{{ house.sales }}条评价</span>
        </span>
      </div>

      <h3 class="house-name">{{ house.name }}</h3>

      <div class="house-info">
        <span v-if="house.capacity != null" class="info-tag">
          <svg width="14" height="14" viewBox="0 0 14 14"><path d="M4 3h6M4 5h4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/><rect x="3" y="7" width="8" height="4" rx="1" stroke="currentColor" stroke-width="1.2"/></svg>
          宜住{{ house.capacity }}人
        </span>
        <span v-if="house.type" class="info-tag">{{ house.type }}</span>
      </div>

      <div class="house-footer">
        <div class="house-price">
          <strong>&yen;{{ displayPrice }}</strong>
          <span>/晚</span>
        </div>
        <el-button
          v-if="showActions"
          type="primary"
          size="small"
          class="book-btn"
          @click="handleBook"
        >
          预约
        </el-button>
      </div>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="skeleton-overlay">
      <div class="skeleton-shimmer"></div>
    </div>
  </div>
</template>

<style scoped>
.house-card {
  position: relative;
  background: var(--c-surface);
  border: 1px solid rgb(229 232 236 / 70%);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
}
.house-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-4px);
}

/* Cover */
.house-cover {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, var(--c-line-light) 0%, var(--c-bg) 100%);
}
.house-cover img {
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}
.house-card:hover .house-cover img {
  transform: scale(1.04);
}

.cover-badge {
  position: absolute; top: 10px; left: 10px;
  padding: 4px 10px;
  font-size: 12px; font-weight: 600;
  color: #fff;
  background: rgb(0 0 0 / 55%);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-sm);
  letter-spacing: 0.3px;
}

.cover-unavailable {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  background: rgb(255 255 255 / 70%);
  backdrop-filter: blur(2px);
}
.cover-unavailable span {
  padding: 6px 16px;
  font-size: 14px; font-weight: 600; color: var(--c-red);
  background: var(--c-red-bg);
  border-radius: var(--radius-full);
}

.favorite-btn {
  position: absolute; top: 10px; right: 10px;
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  background: rgb(255 255 255 / 88%);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px; color: var(--c-muted-light);
  transition: all var(--transition-bounce);
  box-shadow: var(--shadow-sm);
  opacity: 0;
}
.house-card:hover .favorite-btn { opacity: 1; }
.favorite-btn.favorited { opacity: 1; color: var(--c-coral); }
.favorite-btn:hover { transform: scale(1.15); color: var(--c-coral); }

/* Body */
.house-meta {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 8px;
}
.house-city {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: var(--c-muted);
}
.house-rating {
  display: flex; align-items: center; gap: 3px;
  font-size: 13px; font-weight: 600; color: var(--c-amber);
}
.house-rating .el-icon { font-size: 14px; }
.sales-count { font-weight: 400; color: var(--c-muted); font-size: 12px; margin-left: 2px; }

.house-name {
  margin: 0 0 10px;
  font-size: 16px; font-weight: 600;
  color: var(--c-ink);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.house-info { display: flex; gap: 10px; margin-bottom: 14px; }
.info-tag {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: var(--c-muted); letter-spacing: 0.2px;
}
.info-tag svg { color: var(--c-muted-light); }

.house-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid var(--c-line-light);
}

.house-price {
  display: flex; align-items: baseline; gap: 2px;
  font-size: 13px; color: var(--c-muted);
}
.house-price strong {
  font-size: 20px; font-weight: 700; color: var(--c-price);
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Size variants */
.house-card--small .house-name { font-size: 15px; }
.house-card--small .house-price strong { font-size: 18px; }
.house-card--small .house-cover { height: 180px; }

/* Skeleton */
.skeleton-overlay {
  position: absolute; inset: 0;
  background: var(--c-surface);
  overflow: hidden;
}
.skeleton-shimmer {
  width: 100%; height: 100%;
  background: linear-gradient(90deg,
    var(--c-line-light) 25%,
    var(--c-bg) 50%,
    var(--c-line-light) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@media (max-width: 640px) {
  .house-card:hover { transform: none; }
  .favorite-btn { opacity: 1; }
}
</style>
