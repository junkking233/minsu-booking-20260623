<script setup lang="ts">
/**
 * MetricCard — 指标卡片组件
 * 用于管理后台数据看板和统计展示
 */
import { computed } from 'vue';
import type { Component } from 'vue';

export type MetricColor = 'primary' | 'coral' | 'sage' | 'amber' | 'blue' | 'indigo' | 'red';

const props = withDefaults(defineProps<{
  label: string;
  value: string | number;
  icon?: Component;
  color?: MetricColor;
  prefix?: string;
  suffix?: string;
  trend?: string;        // 趋势变化，如 "+12.5%"
  trendUp?: boolean;      // true=上升(绿), false=下降(红)
  loading?: boolean;
}>(), {
  color: 'primary',
  loading: false,
});

const colorMap: Record<MetricColor, { bg: string; text: string; gradient: string }> = {
  primary: { bg: 'var(--c-primary-bg)', text: 'var(--c-primary)', gradient: 'linear-gradient(135deg, #0284c7, #0ea5e9)' },
  coral:   { bg: 'var(--c-coral-bg)', text: 'var(--c-coral)', gradient: 'linear-gradient(135deg, #d95a3e, #f5a78a)' },
  sage:    { bg: 'var(--c-sage-bg)', text: 'var(--c-sage)', gradient: 'linear-gradient(135deg, #6b9b7f, #8fbc9a)' },
  amber:   { bg: 'var(--c-amber-bg)', text: 'var(--c-amber)', gradient: 'linear-gradient(135deg, #e8a84a, #fbbf24)' },
  blue:    { bg: 'var(--c-blue-bg)', text: 'var(--c-blue)', gradient: 'linear-gradient(135deg, #2563eb, #60a5fa)' },
  indigo:  { bg: '#eef2ff', text: '#4f46e5', gradient: 'linear-gradient(135deg, #4f46e5, #818cf8)' },
  red:     { bg: 'var(--c-red-bg)', text: 'var(--c-red)', gradient: 'linear-gradient(135deg, #dc2626, #f87171)' },
};

const styles = computed(() => colorMap[props.color]);

const displayValue = computed(() => {
  const v = props.value;
  const pre = props.prefix || '';
  const suf = props.suffix || '';
  return `${pre}${v}${suf}`;
});

const trendColor = computed(() => {
  if (props.trend == null) return '';
  return props.trendUp ? 'var(--c-green)' : 'var(--c-red)';
});
</script>

<template>
  <div class="metric-card" :class="{ 'is-loading': loading }">
    <div class="metric-top-row">
      <div class="metric-icon" :style="{ background: styles.gradient }">
        <el-icon v-if="icon"><component :is="icon" /></el-icon>
        <slot v-else name="icon" />
      </div>
      <template v-if="loading">
        <div class="skeleton-value"></div>
        <div class="skeleton-label"></div>
      </template>
      <template v-else>
        <div class="metric-value">{{ displayValue }}</div>
        <div class="metric-label">{{ label }}</div>
      </template>
    </div>
    <div v-if="trend" class="metric-trend" :style="{ color: trendColor }">
      {{ trend }}
    </div>
  </div>
</template>

<style scoped>
.metric-card {
  position: relative;
  padding: 20px 24px;
  background: var(--c-surface);
  border: 1px solid rgb(229 232 236 / 60%);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
  overflow: hidden;
}
.metric-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: v-bind('styles.gradient');
  opacity: 0; transition: opacity var(--transition-base);
}
.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card-hover);
}
.metric-card:hover::before { opacity: 1; }

.metric-icon {
  width: 44px; height: 44px;
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--radius-md);
  color: #fff;
  font-size: 20px;
  margin-bottom: 16px;
}

.metric-value {
  font-size: 28px; font-weight: 800;
  color: var(--c-ink);
  line-height: 1.1;
  margin-bottom: 4px;
  letter-spacing: -0.5px;
  font-feature-settings: "tnum";
}

.metric-label {
  font-size: 13px; color: var(--c-muted);
  font-weight: 500; letter-spacing: 0.2px;
}

.metric-trend {
  position: absolute; top: 24px; right: 24px;
  font-size: 13px; font-weight: 600;
}

/* Skeleton */
.skeleton-value {
  width: 60%; height: 28px;
  background: var(--c-line-light);
  border-radius: var(--radius-sm);
  margin-bottom: 6px;
  animation: shimmer 1.5s infinite;
}
.skeleton-label {
  width: 40%; height: 14px;
  background: var(--c-line-light);
  border-radius: var(--radius-sm);
  animation: shimmer 1.5s infinite;
}
</style>
