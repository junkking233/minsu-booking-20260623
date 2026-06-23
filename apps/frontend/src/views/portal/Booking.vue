<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { houseApi } from '@/api/houseApi';
import { orderApi } from '@/api/orderApi';

const route = useRoute();
const router = useRouter();
const houseId = Number(route.params.houseId);

interface HouseInfo {
  id: number;
  name: string;
  city: string;
  price: number;
  cleanFee: number;
  type: string;
  capacity: number;
  coverImage: string;
  address: string;
}

interface PriceDetail {
  roomFee: number;
  cleanFee: number;
  discount: number;
  total: number;
}

const house = ref<HouseInfo | null>(null);
const loading = ref(false);
const submitting = ref(false);
const availability = ref<{ available: boolean; nights: number; priceDetail: PriceDetail } | null>(null);

const form = ref({
  checkIn: (route.query.checkIn as string) || '',
  checkOut: (route.query.checkOut as string) || '',
  guests: Number(route.query.guests) || 1,
  guestName: '',
  phone: '',
  note: '',
});

async function loadHouse() {
  loading.value = true;
  try {
    const data = await houseApi.getDetail(houseId) as HouseInfo;
    house.value = data;
  } catch (e) {
    ElMessage.error('加载房源信息失败');
  } finally {
    loading.value = false;
  }
}

async function checkAvailability() {
  if (!form.value.checkIn || !form.value.checkOut || !form.value.guests) return;
  try {
    const data = await houseApi.checkAvailability(houseId, {
      checkIn: form.value.checkIn,
      checkOut: form.value.checkOut,
      guests: form.value.guests,
    }) as any;
    availability.value = data;
  } catch (e) {
    availability.value = null;
    ElMessage.error(e instanceof Error ? e.message : '校验失败');
  }
}

async function submitOrder() {
  if (!form.value.guestName) {
    ElMessage.warning('请填写联系人姓名');
    return;
  }
  if (!form.value.phone || !/^1\d{10}$/.test(form.value.phone)) {
    ElMessage.warning('请填写正确的手机号');
    return;
  }
  if (!form.value.checkIn || !form.value.checkOut) {
    ElMessage.warning('请选择入住和离店日期');
    return;
  }
  submitting.value = true;
  try {
    const result = await orderApi.create({
      houseId,
      checkIn: form.value.checkIn,
      checkOut: form.value.checkOut,
      guests: form.value.guests,
      guestName: form.value.guestName,
      phone: form.value.phone,
      note: form.value.note,
    }) as any;
    ElMessage.success('预约成功');
    router.push(`/portal/orders/${result.orderNo}`);
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '预约失败');
  } finally {
    submitting.value = false;
  }
}

onMounted(loadHouse);
</script>

<template>
  <div class="booking-page" v-loading="loading">
    <div class="page-inner">
      <h1 class="page-title">预约下单</h1>

      <div class="booking-grid" v-if="house">
        <!-- 左侧表单 -->
        <div class="booking-main">
          <!-- 房源信息概要 -->
          <el-card class="house-summary">
            <div class="summary-top">
              <img :src="house.coverImage || '/placeholder.svg'" class="summary-img" />
              <div class="summary-info">
                <h3>{{ house.name }}</h3>
                <p>{{ house.city }} · {{ house.type }} · 可住 {{ house.capacity }} 人</p>
                <p class="summary-addr">{{ house.address }}</p>
              </div>
            </div>
          </el-card>

          <!-- 订单表单 -->
          <el-card class="order-form">
            <h3>填写订单信息</h3>
            <el-form :model="form" label-width="80px" label-position="top">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="入住日期">
                    <el-date-picker
                      v-model="form.checkIn"
                      type="date"
                      placeholder="选择入住日期"
                      style="width: 100%"
                      @change="checkAvailability"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="离店日期">
                    <el-date-picker
                      v-model="form.checkOut"
                      type="date"
                      placeholder="选择离店日期"
                      style="width: 100%"
                      @change="checkAvailability"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="入住人数">
                <el-input-number v-model="form.guests" :min="1" :max="house.capacity" @change="checkAvailability" />
              </el-form-item>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="联系人姓名">
                    <el-input v-model="form.guestName" placeholder="请输入姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号">
                    <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="备注">
                <el-input v-model="form.note" type="textarea" :rows="3" placeholder="有任何特殊需求请备注" />
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- 右侧费用面板 -->
        <div class="booking-sidebar">
          <el-card class="price-card">
            <h3>费用明细</h3>
            <template v-if="availability">
              <div class="price-row">
                <span>房费</span>
                <span>&yen;{{ availability.priceDetail.roomFee }}</span>
              </div>
              <div class="price-row">
                <span>清洁费</span>
                <span>&yen;{{ availability.priceDetail.cleanFee }}</span>
              </div>
              <div class="price-row" v-if="availability.priceDetail.discount > 0">
                <span>优惠</span>
                <span class="discount">-&yen;{{ availability.priceDetail.discount }}</span>
              </div>
              <div class="price-row price-total">
                <span>合计</span>
                <strong>&yen;{{ availability.priceDetail.total }}</strong>
              </div>
            </template>
            <div v-else class="price-empty">
              请选择日期和人数后查看费用
            </div>

            <div class="price-hint" v-if="availability && !availability.available">
              该时段不可预订
            </div>

            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="submitting"
              :disabled="!availability || !availability.available"
              @click="submitOrder"
            >
              提交订单
            </el-button>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.booking-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  margin: 0 0 24px;
  font-size: 24px;
  font-weight: 800;
}

.booking-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

.house-summary {
  margin-bottom: 20px;
}

.summary-top {
  display: flex;
  gap: 16px;
}

.summary-img {
  width: 160px;
  height: 120px;
  object-fit: cover;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.summary-info h3 {
  margin: 0 0 6px;
  font-size: 17px;
}

.summary-info p {
  margin: 4px 0;
  color: var(--c-muted);
  font-size: 13px;
}

.summary-addr {
  margin-top: 8px !important;
}

.order-form h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

/* Sidebar */
.price-card {
  position: sticky;
  top: 80px;
}

.price-card h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: var(--c-body);
}

.price-total {
  border-top: 1px solid var(--c-line);
  margin-top: 4px;
  padding-top: 12px;
  font-size: 16px;
}

.price-total strong {
  color: var(--c-red);
  font-size: 20px;
}

.discount {
  color: var(--c-green);
}

.price-empty {
  padding: 20px 0;
  text-align: center;
  color: var(--c-muted);
  font-size: 14px;
}

.price-hint {
  text-align: center;
  color: var(--c-red);
  font-size: 14px;
  padding: 8px 0;
  font-weight: 600;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  border-radius: var(--radius-md);
  margin-top: 16px;
}

@media (max-width: 768px) {
  .booking-grid {
    grid-template-columns: 1fr;
  }

  .summary-top {
    flex-direction: column;
  }

  .summary-img {
    width: 100%;
    height: 180px;
  }
}
</style>
