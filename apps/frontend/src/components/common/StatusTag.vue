<script setup lang="ts">
/**
 * StatusTag — 统一状态标签
 * 集中管理订单/房源/评价等状态的颜色映射
 */
import { computed } from 'vue';

export type OrderStatus =
  | 'PENDING' | 'WAIT_PAY' | 'BOOKED' | 'CHECKED_IN'
  | 'COMPLETED' | 'REVIEWED' | 'CANCELLED'
  | 'REFUNDING' | 'REFUNDED';

withDefaults(defineProps<{
  status: string;
  type?: 'order' | 'house' | 'review' | 'user';
  size?: 'default' | 'small';
}>(), {
  type: 'order',
  size: 'default',
});

// 标签文本映射
const statusLabel: Record<string, string> = {
  PENDING: '待确认', WAIT_PAY: '待支付', BOOKED: '已预订',
  CHECKED_IN: '入住中', COMPLETED: '已完成', REVIEWED: '已评价',
  CANCELLED: '已取消', REFUNDING: '退款中', REFUNDED: '已退款',
  ACTIVE: '正常', DISABLED: '已禁用',
  VISIBLE: '可见', HIDDEN: '隐藏',
  AVAILABLE: '可订', LOCKED: '已锁定', BOOKED_DAYS: '已订',
  UP: '已上架', DOWN: '已下架',
};

// Element Plus tag 类型
const tagType: Record<string, string> = {
  WAIT_PAY: 'danger', CANCELLED: 'info', REFUNDING: 'warning',
  REFUNDED: 'info', DISABLED: 'info', HIDDEN: 'warning',
  LOCKED: 'warning', UP: 'success', DOWN: 'info',
};
</script>

<template>
  <span :class="['status-tag', `status-${type}`, `sz-${size}`]" :data-status="status">
    <el-tag
      :type="tagType[status] || ''"
      :size="size"
      :disable-transitions="true"
    >
      {{ statusLabel[status] || status }}
    </el-tag>
  </span>
</template>

<style scoped>
.status-tag {
  --s: 0;
  display: inline-flex;
  align-items: center;
}

/* 动态驱动全局 status-* 类（在 styles.css 中定义颜色） */
.status-tag.status-order { --s: 1; }
.status-tag.status-house { --s: 1; }

/* Dot indicator before tag */
.status-tag::before {
  content: '';
  display: inline-block;
  width: 6px; height: 6px;
  border-radius: 50%;
  margin-right: 6px;
  background: currentColor;
  opacity: 0.5;
}
</style>
