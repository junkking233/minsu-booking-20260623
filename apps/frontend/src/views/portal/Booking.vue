<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Lock } from '@element-plus/icons-vue';
import { houseApi } from '@/api/houseApi';
import { orderApi } from '@/api/orderApi';
import { formatDateParam } from '@/utils/date';

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
const availabilityMessage = ref('');

const form = ref({
  checkIn: formatDateParam(route.query.checkIn),
  checkOut: formatDateParam(route.query.checkOut),
  guests: Number(route.query.guests) || 1,
  guestName: '',
  phone: '',
  note: '',
});

function todayStart() {
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  return now;
}

function normalizeAvailability(data: any) {
  const nights = Number(data.nights || 0);
  const roomFee = Number(data.priceDetail?.roomFee ?? (data.price || 0) * nights);
  const cleanFee = Number(data.priceDetail?.cleanFee ?? data.cleanFee ?? 0);
  const discount = Number(data.priceDetail?.discount ?? 0);
  const total = Number(data.priceDetail?.total ?? Math.max(roomFee + cleanFee - discount, 0));
  return {
    available: Boolean(data.available),
    nights,
    priceDetail: { roomFee, cleanFee, discount, total },
  };
}

function isValidDateRange() {
  if (!form.value.checkIn || !form.value.checkOut) {
    availabilityMessage.value = '';
    return false;
  }
  const checkIn = new Date(form.value.checkIn);
  const checkOut = new Date(form.value.checkOut);
  if (checkIn < todayStart()) {
    availabilityMessage.value = '入住日期不能早于今天';
    availability.value = null;
    return false;
  }
  if (checkOut <= checkIn) {
    availabilityMessage.value = '离店日期必须晚于入住日期';
    availability.value = null;
    return false;
  }
  return true;
}

async function loadHouse() {
  loading.value = true;
  try {
    const data = await houseApi.getDetail(houseId) as HouseInfo;
    house.value = data;
    await checkAvailability();
  } catch (e) {
    ElMessage.error('加载房源信息失败');
  } finally {
    loading.value = false;
  }
}

async function checkAvailability() {
  if (!form.value.checkIn || !form.value.checkOut || !form.value.guests) return;
  if (!isValidDateRange()) return;
  try {
    const data = await houseApi.checkAvailability(houseId, {
      checkIn: formatDateParam(form.value.checkIn),
      checkOut: formatDateParam(form.value.checkOut),
      guests: form.value.guests,
    }) as any;
    availability.value = normalizeAvailability(data);
    availabilityMessage.value = data.message || (availability.value.available ? '' : '该时段不可预订');
  } catch (e) {
    availability.value = null;
    availabilityMessage.value = e instanceof Error ? e.message : '校验失败';
    ElMessage.error(availabilityMessage.value);
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
  if (!isValidDateRange()) {
    ElMessage.warning(availabilityMessage.value);
    return;
  }
  if (!availability.value) {
    await checkAvailability();
  }
  if (!availability.value?.available) {
    ElMessage.warning(availabilityMessage.value || '该时段不可预订');
    return;
  }
  submitting.value = true;
  try {
    const result = await orderApi.create({
      houseId,
      checkIn: formatDateParam(form.value.checkIn),
      checkOut: formatDateParam(form.value.checkOut),
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
                      value-format="YYYY-MM-DD"
                      placeholder="选择入住日期"
                      style="width: 100%"
                      :disabled-date="(d: Date) => d.getTime() < todayStart().getTime()"
                      @change="checkAvailability"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="离店日期">
                    <el-date-picker
                      v-model="form.checkOut"
                      type="date"
                      value-format="YYYY-MM-DD"
                      placeholder="选择离店日期"
                      style="width: 100%"
                      :disabled-date="(d: Date) => !form.checkIn || d.getTime() <= new Date(form.checkIn).getTime()"
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
              {{ availabilityMessage || '请选择日期和人数后查看费用' }}
            </div>

            <div class="price-hint" v-if="availability && !availability.available">
              {{ availabilityMessage || '该时段不可预订' }}
            </div>

            <el-button
              type="primary"
              size="large"
              class="submit-btn btn-coral"
              :loading="submitting"
              :disabled="!availability || !availability.available"
              @click="submitOrder"
            >
              <el-icon><Lock /></el-icon> 提交订单
            </el-button>

            <div class="trust-strip">
              <span class="trust-item">✓ 免费取消</span>
              <span class="trust-item">✓ 安全支付</span>
            </div>
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
  color: var(--c-price);
  font-size: 22px;
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
  margin-top: 8px;
  display: flex; align-items: center; justify-content: center; gap: 8px;
}

.trust-strip {
  display: flex; justify-content: center; gap: 20px;
  margin-top: 14px; padding-top: 14px;
  border-top: 1px solid var(--c-line-light);
}
.trust-item { font-size: 12px; color: var(--c-sage); font-weight: 600; }

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
