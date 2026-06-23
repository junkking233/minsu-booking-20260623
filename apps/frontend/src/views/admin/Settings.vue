<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { configApi, type SystemSettings } from '@/api/configApi';

interface CityItem { id: number; name: string; sort: number; }
interface TagItem { id: number; name: string; }

const cities = ref<CityItem[]>([]);
const tags = ref<TagItem[]>([]);
const settings = ref<SystemSettings>({
  defaultDiscount: 30,
  payTimeoutMinutes: 30,
  freeCancelHours: 24,
  reviewAvailableDays: 30,
});

const newCity = ref('');
const newTag = ref('');
const loading = ref(false);
const saving = ref(false);

async function loadData() {
  loading.value = true;
  try {
    const [citiesData, tagsData, settingsData] = await Promise.all([
      configApi.cities(),
      configApi.tags(),
      configApi.adminGetSettings(),
    ]);
    cities.value = citiesData as CityItem[];
    tags.value = tagsData as TagItem[];
    settings.value = settingsData as SystemSettings;
  } catch (e) {
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
}

async function addCity() {
  if (!newCity.value.trim()) { ElMessage.warning('请输入城市名称'); return; }
  try {
    await configApi.adminAddCity(newCity.value.trim());
    ElMessage.success('添加成功');
    newCity.value = '';
    loadData();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '添加失败');
  }
}

async function deleteCity(city: CityItem) {
  try {
    await ElMessageBox.confirm(`确定删除城市「${city.name}」？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await configApi.adminDeleteCity(city.id);
    ElMessage.success('已删除');
    loadData();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '删除失败');
  }
}

async function addTag() {
  if (!newTag.value.trim()) { ElMessage.warning('请输入标签名称'); return; }
  try {
    await configApi.adminAddTag(newTag.value.trim());
    ElMessage.success('添加成功');
    newTag.value = '';
    loadData();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '添加失败');
  }
}

async function deleteTag(tag: TagItem) {
  try {
    await ElMessageBox.confirm(`确定删除标签「${tag.name}」？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await configApi.adminDeleteTag(tag.id);
    ElMessage.success('已删除');
    loadData();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '删除失败');
  }
}

async function saveSettings() {
  saving.value = true;
  try {
    await configApi.adminUpdateSettings(settings.value);
    ElMessage.success('保存成功');
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

onMounted(loadData);
</script>

<template>
  <div class="settings-page" v-loading="loading">
    <div class="settings-grid">
      <!-- 城市管理 -->
      <el-card class="settings-card">
        <h3>城市管理</h3>
        <div class="tag-list">
          <el-tag
            v-for="c in cities"
            :key="c.id"
            closable
            size="large"
            class="config-tag"
            @close="deleteCity(c)"
          >
            {{ c.name }}
          </el-tag>
        </div>
        <div class="add-row">
          <el-input v-model="newCity" placeholder="新城市名称" size="small" class="add-input" @keyup.enter="addCity" />
          <el-button type="primary" :icon="Plus" size="small" @click="addCity">添加</el-button>
        </div>
      </el-card>

      <!-- 设施标签管理 -->
      <el-card class="settings-card">
        <h3>设施标签管理</h3>
        <div class="tag-list">
          <el-tag
            v-for="t in tags"
            :key="t.id"
            closable
            size="large"
            class="config-tag"
            @close="deleteTag(t)"
          >
            {{ t.name }}
          </el-tag>
        </div>
        <div class="add-row">
          <el-input v-model="newTag" placeholder="新标签名称" size="small" class="add-input" @keyup.enter="addTag" />
          <el-button type="primary" :icon="Plus" size="small" @click="addTag">添加</el-button>
        </div>
      </el-card>

      <!-- 平台规则设置 -->
      <el-card class="settings-card settings-full">
        <h3>平台规则设置</h3>
        <el-form :model="settings" label-width="140px">
          <el-form-item label="默认优惠金额（元）">
            <el-input-number v-model="settings.defaultDiscount" :min="0" />
          </el-form-item>
          <el-form-item label="待支付超时（分钟）">
            <el-input-number v-model="settings.payTimeoutMinutes" :min="1" />
          </el-form-item>
          <el-form-item label="免费取消时限（小时）">
            <el-input-number v-model="settings.freeCancelHours" :min="0" />
          </el-form-item>
          <el-form-item label="评价保留天数">
            <el-input-number v-model="settings.reviewAvailableDays" :min="1" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveSettings">保存配置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  max-width: 1000px;
}

.settings-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.settings-card h3 {
  margin: 0 0 16px;
  font-size: 17px;
}

.settings-full {
  grid-column: 1 / -1;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
  min-height: 36px;
}

.config-tag {
  font-size: 14px;
  padding: 6px 14px;
  border-radius: var(--radius-md);
}

.add-row {
  display: flex;
  gap: 8px;
}

.add-input {
  flex: 1;
  max-width: 200px;
}

@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
