<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { favoriteApi } from '@/api/favoriteApi';
import HouseCard from '@/components/common/HouseCard.vue';
import EmptyState from '@/components/common/EmptyState.vue';

const router = useRouter();

interface FavoriteItem {
  id: number; houseId: number; houseName: string;
  houseCoverImage: string; houseCity: string; housePrice: number;
  houseRating: number; houseStatus: string; createTime: string;
}

const favorites = ref<FavoriteItem[]>([]);
const loading = ref(false);

async function loadFavorites() {
  loading.value = true;
  try { favorites.value = (await favoriteApi.myList()) as FavoriteItem[] || []; }
  catch { ElMessage.error('加载收藏失败'); }
  finally { loading.value = false; }
}

async function handleRemove(fav: FavoriteItem) {
  try {
    await favoriteApi.remove(fav.houseId);
    ElMessage.success('已取消收藏');
    loadFavorites();
  } catch (e: any) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

function goDetail(id: number | string) { router.push(`/portal/houses/${id}`); }
function goBooking(house: any) { router.push(`/portal/booking/${house.id || house.houseId}`); }

onMounted(loadFavorites);
</script>

<template>
  <div class="favorites-page">
    <div class="page-header">
      <h1 class="page-title">我的收藏</h1>
      <span class="fav-count" v-if="favorites.length">共 {{ favorites.length }} 套</span>
    </div>

    <div v-loading="loading">
      <div class="fav-grid" v-if="favorites.length > 0">
        <div class="fav-card-wrapper" v-for="fav in favorites" :key="fav.id">
          <div class="fav-remove" @click="handleRemove(fav)" title="取消收藏">
            <el-icon :size="14"><el-icon><svg viewBox="0 0 24 24" width="14" height="14"><path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></el-icon></el-icon>
          </div>
          <HouseCard
            size="small"
            :house="{
              id: fav.houseId, name: fav.houseName, city: fav.houseCity,
              price: fav.housePrice, rating: fav.houseRating,
              coverImage: fav.houseCoverImage,
              status: fav.houseStatus === 'OFF' ? 'UNAVAILABLE' : undefined,
            }"
            :favorited="true"
            :show-actions="true"
            @click="goDetail"
            @book="goBooking"
            @toggle-favorite="(h: any) => handleRemove(fav)"
          />
        </div>
      </div>

      <EmptyState v-else-if="!loading" type="favorites" :show-action="true" @action="router.push('/portal/houses')">
        <template #action-text>去挑选民宿</template>
      </EmptyState>
    </div>
  </div>
</template>

<style scoped>
.favorites-page { max-width: 1000px; margin: 0 auto; padding: 24px 20px; }

.page-header { display: flex; align-items: baseline; gap: 12px; margin-bottom: 24px; }
.page-title { margin: 0; font-size: 26px; font-weight: 800; letter-spacing: -0.5px; color: var(--c-ink); }
.fav-count { font-size: 14px; color: var(--c-muted); }

.fav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.fav-card-wrapper { position: relative; }
.fav-remove {
  position: absolute; top: 10px; right: 10px; z-index: 10;
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  background: rgb(255 255 255 / 85%);
  backdrop-filter: blur(4px);
  border-radius: 50%;
  cursor: pointer;
  color: var(--c-muted);
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
}
.fav-remove:hover { background: var(--c-red-bg); color: var(--c-red); }

@media (max-width: 768px) {
  .fav-grid { grid-template-columns: 1fr; }
}
</style>
