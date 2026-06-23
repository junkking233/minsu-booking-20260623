<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search } from '@element-plus/icons-vue';
import { houseApi, type HouseFormData } from '@/api/houseApi';
import { configApi } from '@/api/configApi';
import { uploadApi } from '@/api/uploadApi';

const router = useRouter();

interface CityItem { id: number; name: string; }
interface TagItem { id: number; name: string; }
interface HouseRecord {
  id: number;
  name: string;
  city: string;
  price: number;
  cleanFee: number;
  type: string;
  rooms: string;
  area: string;
  capacity: number;
  coverImage: string;
  status: string;
  rating: number;
  sales: number;
  createTime: string;
}

const houses = ref<HouseRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const cities = ref<CityItem[]>([]);
const tags = ref<TagItem[]>([]);

const filters = reactive({ keyword: '', status: '' });
const pagination = reactive({ page: 1, pageSize: 20 });

const dialogVisible = ref(false);
const dialogTitle = ref('新增房源');
const editingId = ref<number | null>(null);
const form = reactive<HouseFormData>({
  name: '', city: '', price: 0, cleanFee: 0, type: '', rooms: '', area: '',
  capacity: 1, address: '', host: '', coverImage: '', facilities: [], description: '', rule: '',
});

const typeOptions = ['单间', '一居室', '两居室', '整套'];

async function loadData() {
  loading.value = true;
  try {
    const [housesData, citiesData, tagsData] = await Promise.all([
      houseApi.adminList({ ...filters, page: pagination.page, pageSize: pagination.pageSize }),
      configApi.cities(),
      configApi.tags(),
    ]);
    const hd = housesData as { records: HouseRecord[]; total: number };
    houses.value = hd.records || [];
    total.value = hd.total || 0;
    cities.value = citiesData as CityItem[];
    tags.value = tagsData as TagItem[];
  } catch (e) {
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  pagination.page = 1;
  loadData();
}

function onReset() {
  filters.keyword = '';
  filters.status = '';
  onSearch();
}

function openCreate() {
  editingId.value = null;
  dialogTitle.value = '新增房源';
  Object.assign(form, {
    name: '', city: '', price: 0, cleanFee: 0, type: '', rooms: '', area: '',
    capacity: 1, address: '', host: '', coverImage: '', facilities: [], description: '', rule: '',
  });
  dialogVisible.value = true;
}

function openEdit(row: HouseRecord) {
  editingId.value = row.id;
  dialogTitle.value = '编辑房源';
  Object.assign(form, {
    name: row.name, city: row.city, price: row.price, cleanFee: row.cleanFee,
    type: row.type, rooms: row.rooms, area: row.area, capacity: row.capacity,
    address: '', host: '', coverImage: row.coverImage, facilities: [], description: '', rule: '',
  });
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.name || !form.address || !form.host) {
    ElMessage.warning('请补充房源名称、地址和房东');
    return;
  }
  try {
    if (editingId.value) {
      await houseApi.adminUpdate(editingId.value, { ...form });
      ElMessage.success('修改成功');
    } else {
      await houseApi.adminCreate({ ...form });
      ElMessage.success('新增成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

async function handleToggleStatus(row: HouseRecord) {
  const newStatus = row.status === 'ON' ? 'OFF' : 'ON';
  const action = newStatus === 'ON' ? '上架' : '下架';
  try {
    await ElMessageBox.confirm(`确定${action}该房源？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' });
    await houseApi.adminToggleStatus(row.id, newStatus);
    ElMessage.success(`${action}成功`);
    loadData();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '操作失败');
  }
}

async function handleDelete(row: HouseRecord) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」？此操作不可恢复`, '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' });
    await houseApi.adminDelete(row.id);
    ElMessage.success('删除成功');
    loadData();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e instanceof Error ? e.message : '删除失败');
  }
}

async function handleCoverUpload(file: File) {
  try {
    const data = await uploadApi.uploadImage(file) as { url: string };
    form.coverImage = data.url;
    ElMessage.success('封面上传成功');
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '上传失败');
  }
  return false;
}

function goCalendar(houseId: number) {
  router.push(`/admin/calendar/${houseId}`);
}

onMounted(loadData);
</script>

<template>
  <div class="house-manage">
    <!-- 工具栏 -->
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-input v-model="filters.keyword" placeholder="搜索名称/城市" :prefix-icon="Search" clearable class="w-200" @keyup.enter="onSearch" @clear="onSearch" />
        <el-select v-model="filters.status" placeholder="状态" clearable class="w-120" @change="onSearch">
          <el-option label="上架" value="ON" />
          <el-option label="下架" value="OFF" />
        </el-select>
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="onReset">重置</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增房源</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="houses" v-loading="loading" border stripe class="w-full">
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img :src="row.coverImage || '/placeholder.svg'" class="table-img" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="city" label="城市" width="80" />
      <el-table-column prop="price" label="价格" width="90">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column prop="capacity" label="可住" width="60" />
      <el-table-column prop="rating" label="评分" width="70" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ON' ? 'success' : 'info'">{{ row.status === 'ON' ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 'ON' ? 'warning' : 'success'" @click="handleToggleStatus(row)">
            {{ row.status === 'ON' ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" @click="goCalendar(row.id)">房态</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="房源名称">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在城市">
              <el-select v-model="form.city" style="width:100%">
                <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="价格">
              <el-input-number v-model="form.price" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="清洁费">
              <el-input-number v-model="form.cleanFee" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="类型">
              <el-select v-model="form.type" style="width:100%">
                <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="房间">
              <el-input v-model="form.rooms" placeholder="如：1室1厅1卫" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="面积">
              <el-input v-model="form.area" placeholder="如：60㎡" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="可住人数">
              <el-input-number v-model="form.capacity" :min="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="房东">
          <el-input v-model="form.host" />
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="cover-upload">
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
            <el-upload :show-file-list="false" :before-upload="(f: File) => { handleCoverUpload(f); return false; }" accept="image/*">
              <el-button>上传封面</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="配套设施">
          <el-select v-model="form.facilities" multiple placeholder="选择设施标签" style="width:100%">
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="房源介绍">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="入住规则">
          <el-input v-model="form.rule" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.house-manage {
  max-width: 1400px;
}

.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.w-200 { width: 200px; }
.w-120 { width: 120px; }
.w-full { width: 100%; }

.table-img {
  width: 60px;
  height: 45px;
  object-fit: cover;
  border-radius: 4px;
}

.cover-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cover-preview {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  border: 1px solid var(--c-line);
}
</style>
