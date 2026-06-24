<script setup lang="ts">
/**
 * EmptyState — 统一空状态组件
 * 含内联 SVG 插图，支持多种预设类型
 */
import { computed } from 'vue';

type EmptyType = 'default' | 'orders' | 'favorites' | 'search' | 'network';

const props = withDefaults(defineProps<{
  type?: EmptyType;
  title?: string;
  description?: string;
  imageSize?: number;
  showAction?: boolean;
}>(), {
  type: 'default',
  imageSize: 120,
  showAction: false,
});

const emit = defineEmits<{
  action: [];
}>();

const defaultTitle: Record<EmptyType, string> = {
  default: '暂无数据',
  orders: '暂无订单',
  favorites: '暂无收藏',
  search: '暂无搜索结果',
  network: '网络连接异常',
};

const defaultDesc: Record<EmptyType, string> = {
  default: '',
  orders: '还没有预订过民宿，去看看心仪的房源吧',
  favorites: '还没有收藏房源，去挑选喜欢的民宿吧',
  search: '换个关键词试试，或者重新筛选条件',
  network: '请检查网络连接后重试',
};

const displayTitle = computed(() => props.title || defaultTitle[props.type] || defaultTitle.default);
const displayDesc = computed(() => props.description || defaultDesc[props.type] || '');
</script>

<template>
  <div class="empty-state" :style="{ '--img-size': `${imageSize}px` }">
    <!-- 内联 SVG 插图 -->
    <div class="empty-illustration">
      <!-- 无订单: 箱子+绿叶 -->
      <svg v-if="type === 'orders'" viewBox="0 0 120 100" fill="none">
        <rect x="20" y="44" width="80" height="52" rx="8" stroke="#cbd5e1" stroke-width="2" fill="#f8fafc"/>
        <rect x="40" y="36" width="40" height="8" rx="4" fill="#e2e8f0"/>
        <rect x="44" y="40" width="32" height="4" rx="2" fill="#cbd5e1"/>
        <path d="M58 58v8a2 2 0 004 0v-8" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
        <g transform="translate(72, 30)">
          <path d="M2 10c0-3 3-8 8-8 3 0 6 2 8 5 2-3 5-5 8-5 5 0 8 5 8 8" stroke="#22c55e" stroke-width="2" fill="none" stroke-linecap="round"/>
        </g>
      </svg>

      <!-- 无收藏: 心形+房子 -->
      <svg v-else-if="type === 'favorites'" viewBox="0 0 120 100" fill="none">
        <path d="M40 70V38l20 14 20-14v32" stroke="#cbd5e1" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        <rect x="34" y="68" width="32" height="4" rx="1" fill="#e2e8f0"/>
        <path d="M72 28c0-6-4.5-10-10-10s-10 4-10 10c0 8 10 20 10 20s10-12 10-20z"
              stroke="#ea7356" stroke-width="2" fill="#fef3ee" stroke-linecap="round" stroke-linejoin="round"/>
        <circle cx="67" cy="26" r="3" fill="#ea7356" opacity="0.6"/>
      </svg>

      <!-- 无搜索结果: 放大镜+房子 -->
      <svg v-else-if="type === 'search'" viewBox="0 0 120 100" fill="none">
        <circle cx="52" cy="46" r="18" stroke="#cbd5e1" stroke-width="2" fill="#f8fafc"/>
        <line x1="65" y1="59" x2="78" y2="72" stroke="#cbd5e1" stroke-width="2.5" stroke-linecap="round"/>
        <path d="M44 38l6 4 6-4v10H44z" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        <rect x="42" y="46" width="20" height="2" rx="1" fill="#e2e8f0"/>
        <circle cx="48" cy="34" r="2" fill="#94a3b8" opacity="0.5"/>
      </svg>

      <!-- 默认: 文档列表 -->
      <svg v-else viewBox="0 0 120 100" fill="none">
        <rect x="16" y="12" width="88" height="18" rx="6" stroke="#cbd5e1" stroke-width="2" fill="#f8fafc"/>
        <rect x="20" y="16" width="28" height="4" rx="2" fill="#e2e8f0"/>
        <rect x="52" y="16" width="20" height="4" rx="2" fill="#e2e8f0"/>
        <rect x="16" y="38" width="88" height="18" rx="6" stroke="#cbd5e1" stroke-width="2" fill="#f8fafc"/>
        <rect x="20" y="42" width="36" height="4" rx="2" fill="#e2e8f0"/>
        <rect x="16" y="64" width="88" height="18" rx="6" stroke="#cbd5e1" stroke-width="2" fill="#f8fafc"/>
        <rect x="20" y="68" width="22" height="4" rx="2" fill="#e2e8f0"/>
        <rect x="46" y="68" width="32" height="4" rx="2" fill="#e2e8f0" opacity="0.6"/>
      </svg>
    </div>

    <h3 class="empty-title">{{ displayTitle }}</h3>
    <p v-if="displayDesc" class="empty-desc">{{ displayDesc }}</p>

    <slot name="action">
      <el-button v-if="showAction" type="primary" size="default" @click="emit('action')">
        <slot name="action-text">去探索</slot>
      </el-button>
    </slot>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}

.empty-illustration {
  width: var(--img-size, 120px);
  height: auto;
  margin-bottom: 20px;
}
.empty-illustration svg {
  width: 100%;
  height: auto;
}

.empty-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--c-ink-light);
}

.empty-desc {
  margin: 0 0 20px;
  font-size: 14px;
  color: var(--c-muted);
  max-width: 320px;
  line-height: 1.6;
}
</style>
