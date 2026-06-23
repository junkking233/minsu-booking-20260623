<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Star } from '@element-plus/icons-vue';
import { favoriteApi } from '@/api/favoriteApi';

const router = useRouter();

interface FavoriteItem {
  id: number;
  houseId: number;
  houseName: string;
  houseCoverImage: string;
  houseCity: string;
  housePrice: number;
  houseRating: number;
  houseStatus: string;
  createTime: string;
}

const favorites = ref<FavoriteItem[]>([]);
const total = ref(0);
const loading = ref(false);
const page = ref(1);
const pageSize = 20;

async function loadFavorites() {
  loading.value = true;
  try {
    const data = await favoriteApi.myList() as FavoriteItem[];
    favorites.value = data || [];
    total.value = favorites.value.length;
  } catch (e) {
    ElMessage.error('加载收藏失败');
  } finally {
    loading.value = false;
  }
}

async function handleRemove(fav: FavoriteItem) {
  try {
    await ElMessageBox.confirm('确定取消收藏该房源？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await favoriteApi.remove(fav.houseId);
    ElMessage.success('已取消收藏');
    loadFavorites();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

function goDetail(houseId: number) {
  router.push(`/portal/houses/${houseId}`);
}

function goBooking(houseId: number) {
  router.push(`/portal/booking/${houseId}`);
}

function onPageChange(p: number) {
  page.value = p;
  loadFavorites();
}

onMounted(loadFavorites);
</script>

<template>
  <div class="favorites-page">
    <div class="page-inner">
      <h1 class="page-title">我的收藏</h1>

      <div v-loading="loading">
        <div class="fav-grid" v-if="favorites.length > 0">
          <el-card
            v-for="fav in favorites"
            :key="fav.id"
            class="fav-card"
            shadow="hover"
          >
            <img
              :src="fav.houseCoverImage || '/placeholder.svg'"
              class="fav-img"
              @click="goDetail(fav.houseId)"
            />
            <div class="fav-body">
              <h4 class="fav-name" @click="goDetail(fav.houseId)">{{ fav.houseName }}</h4>
              <p class="fav-city">{{ fav.houseCity }}</p>
              <div class="fav-meta">
                <span class="fav-rating"><el-icon><Star /></el-icon>{{ fav.houseRating }}</span>
                <span class="fav-price"><strong>&yen;{{ fav.housePrice }}</strong>/晚</span>
              </div>
              <div class="fav-tags">
                <el-tag v-if="fav.houseStatus === 'OFF'" type="danger" size="small">已下架</el-tag>
              </div>
              <div class="fav-actions">
                <el-button type="danger" size="small" plain @click="handleRemove(fav)">取消收藏</el-button>
                <el-button type="primary" size="small" :disabled="fav.houseStatus === 'OFF'" @click="goBooking(fav.houseId)">
                  预约
                </el-button>
              </div>
            </div>
          </el-card>
        </div>

        <el-empty v-if="!loading && favorites.length === 0" description="暂无收藏">
          <el-button type="primary" @click="router.push('/portal/houses')">去逛逛</el-button>
        </el-empty>

        <div class="pagination-bar" v-if="total > 0">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="onPageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.favorites-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  margin: 0 0 24px;
  font-size: 24px;
  font-weight: 800;
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.fav-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.fav-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  cursor: pointer;
}

.fav-body {
  padding: 14px;
}

.fav-name {
  margin: 0 0 4px;
  font-size: 15px;
  cursor: pointer;
}

.fav-name:hover {
  color: var(--c-primary);
}

.fav-city {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--c-muted);
}

.fav-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.fav-rating {
  display: flex;
  align-items: center;
  gap: 3px;
  color: var(--c-amber);
  font-weight: 600;
  font-size: 13px;
}

.fav-price strong {
  font-size: 17px;
  color: var(--c-red);
}

.fav-price {
  font-size: 12px;
  color: var(--c-muted);
}

.fav-tags {
  margin-bottom: 10px;
}

.fav-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding-top: 10px;
  border-top: 1px solid var(--c-line);
}

@media (max-width: 768px) {
  .fav-grid {
    grid-template-columns: 1fr;
  }
}
</style>
