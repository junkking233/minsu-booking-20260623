<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { reviewApi } from '@/api/reviewApi';
import { uploadApi } from '@/api/uploadApi';

const route = useRoute();
const router = useRouter();
const orderNo = route.params.orderNo as string;

const form = ref({
  rating: 5,
  cleanScore: 5,
  locationScore: 5,
  serviceScore: 5,
  valueScore: 5,
  content: '',
  anonymous: false,
});

const images = ref<string[]>([]);
const uploading = ref(false);
const submitting = ref(false);

const scoreLabels = [
  { key: 'rating', label: '整体评分' },
  { key: 'cleanScore', label: '卫生评分' },
  { key: 'locationScore', label: '位置评分' },
  { key: 'serviceScore', label: '服务评分' },
  { key: 'valueScore', label: '性价比评分' },
];

async function handleUpload(file: File) {
  uploading.value = true;
  try {
    const data = await uploadApi.uploadImage(file) as { url: string };
    images.value.push(data.url);
    ElMessage.success('上传成功');
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '上传失败');
  } finally {
    uploading.value = false;
  }
}

function removeImage(index: number) {
  images.value.splice(index, 1);
}

async function submitReview() {
  if (!form.value.content.trim()) {
    ElMessage.warning('请填写评价内容');
    return;
  }
  submitting.value = true;
  try {
    await reviewApi.create({
      orderNo,
      rating: form.value.rating,
      cleanScore: form.value.cleanScore,
      locationScore: form.value.locationScore,
      serviceScore: form.value.serviceScore,
      valueScore: form.value.valueScore,
      content: form.value.content,
      images: images.value,
      anonymous: form.value.anonymous,
    });
    ElMessage.success('评价成功');
    router.push('/portal/orders');
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '评价失败');
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="review-page">
    <div class="page-inner">
      <h1 class="page-title">发表评价</h1>
      <p class="page-sub">订单号：{{ orderNo }}</p>

      <el-card class="review-card">
        <el-form label-width="100px" label-position="top">
          <!-- 评分 -->
          <h3>评分</h3>
          <div class="score-grid">
            <div v-for="s in scoreLabels" :key="s.key" class="score-item">
              <label>{{ s.label }}</label>
              <el-rate
                v-model="(form as any)[s.key]"
                :max="5"
                size="large"
              />
            </div>
          </div>

          <!-- 评价内容 -->
          <el-form-item label="评价内容" class="mt-20">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="5"
              placeholder="分享您的入住体验..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <!-- 图片上传 -->
          <el-form-item label="上传图片">
            <div class="upload-list">
              <div v-for="(img, i) in images" :key="i" class="upload-item">
                <img :src="img" />
                <el-button class="upload-remove" type="danger" size="small" circle @click="removeImage(i)">×</el-button>
              </div>
              <el-upload
                v-if="images.length < 6"
                :show-file-list="false"
                :before-upload="(file: File) => { handleUpload(file); return false; }"
                accept="image/*"
                :disabled="uploading"
              >
                <div class="upload-trigger">
                  <el-icon><Plus /></el-icon>
                </div>
              </el-upload>
            </div>
          </el-form-item>

          <!-- 匿名 -->
          <el-form-item label="匿名评价">
            <el-switch v-model="form.anonymous" active-text="匿名" inactive-text="实名" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="submitting" @click="submitReview" class="submit-btn">
              提交评价
            </el-button>
            <el-button size="large" @click="router.push('/portal/orders')">返回</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.review-page {
  max-width: 660px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
}

.page-sub {
  margin: 6px 0 24px;
  color: var(--c-muted);
  font-size: 14px;
}

.review-card h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

.score-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.score-item label {
  display: block;
  font-size: 14px;
  color: var(--c-body);
  margin-bottom: 6px;
  font-weight: 500;
}

.mt-20 {
  margin-top: 0;
}

.upload-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.upload-item {
  position: relative;
  width: 100px;
  height: 100px;
}

.upload-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.upload-remove {
  position: absolute;
  top: -8px;
  right: -8px;
}

.upload-trigger {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed var(--c-line);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--c-muted);
  font-size: 28px;
  transition: all var(--transition-fast);
}

.upload-trigger:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.submit-btn {
  min-width: 140px;
  font-weight: 600;
  border-radius: var(--radius-md);
}

@media (max-width: 500px) {
  .score-grid {
    grid-template-columns: 1fr;
  }
}
</style>
