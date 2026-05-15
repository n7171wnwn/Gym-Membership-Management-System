<template>
  <el-card class="gym-page-card">
    <template #header>
      <span>{{ auth.isCoach ? "我的课程" : "课程管理" }}</span>
      <el-button v-if="auth.canAddCourse" type="primary" style="float: right" @click="openAdd">新增课程</el-button>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="名称" min-width="120" />
      <el-table-column prop="slot" label="时间" width="140" />
      <el-table-column label="分类" width="100">
        <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
      </el-table-column>
      <el-table-column prop="capacity" label="上限" width="80" />
      <el-table-column prop="remainingSlots" label="余量" width="80" />
      <el-table-column label="教练" width="120">
        <template #default="{ row }">{{ row.coach?.name || "—" }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ row.enabled === false ? "下架" : "上架" }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="auth.canEditCourse && !auth.isCoach" type="primary" link @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlgAdd" title="新增课程" width="480px" destroy-on-close @closed="resetAdd">
      <el-form :model="formAdd" label-width="100px">
        <el-form-item label="课程名称"><el-input v-model="formAdd.title" /></el-form-item>
        <el-form-item label="时间/时段"><el-input v-model="formAdd.slot" placeholder="如 周一 18:00" /></el-form-item>
        <el-form-item label="人数上限"><el-input-number v-model="formAdd.capacity" :min="1" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="formAdd.category" style="width: 100%">
            <el-option label="瑜伽" value="YOGA" />
            <el-option label="有氧" value="AEROBIC" />
            <el-option label="力量" value="STRENGTH" />
            <el-option label="私教" value="PRIVATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="教练">
          <el-select v-model="formAdd.coachId" filterable style="width: 100%">
            <el-option v-for="c in coaches" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgAdd = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveAdd">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dlgEdit" title="编辑课程" width="480px" destroy-on-close>
      <el-form :model="formEdit" label-width="100px">
        <el-form-item label="课程名称"><el-input v-model="formEdit.title" /></el-form-item>
        <el-form-item label="时间/时段"><el-input v-model="formEdit.slot" /></el-form-item>
        <el-form-item label="人数上限"><el-input-number v-model="formEdit.capacity" :min="1" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="formEdit.category" style="width: 100%">
            <el-option label="瑜伽" value="YOGA" />
            <el-option label="有氧" value="AEROBIC" />
            <el-option label="力量" value="STRENGTH" />
            <el-option label="私教" value="PRIVATE" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="auth.canEditCourse" label="教练">
          <el-select v-model="formEdit.coachId" filterable style="width: 100%">
            <el-option v-for="c in coaches" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上架">
          <el-switch v-model="formEdit.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgEdit = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import api from "../api/http";
import { useAuthStore } from "../stores/auth";
import { categoryLabel } from "../utils/labels";

const auth = useAuthStore();
const list = ref([]);
const coaches = ref([]);
const loading = ref(false);
const saving = ref(false);
const dlgAdd = ref(false);
const dlgEdit = ref(false);
const formAdd = reactive({
  title: "",
  slot: "",
  capacity: 10,
  category: "YOGA",
  coachId: null
});
const formEdit = reactive({
  id: null,
  title: "",
  slot: "",
  capacity: 10,
  category: "YOGA",
  coachId: null,
  enabled: true
});

async function load() {
  loading.value = true;
  try {
    if (auth.isCoach) {
      const { data } = await api.get("/manage/coach/courses");
      list.value = data;
    } else {
      const { data } = await api.get("/courses");
      list.value = data;
    }
  } finally {
    loading.value = false;
  }
}

async function loadCoaches() {
  if (!auth.canFullManage) return;
  try {
    const { data } = await api.get("/coaches");
    coaches.value = data;
  } catch {
    coaches.value = [];
  }
}

function openAdd() {
  resetAdd();
  dlgAdd.value = true;
}
function resetAdd() {
  formAdd.title = "";
  formAdd.slot = "";
  formAdd.capacity = 10;
  formAdd.category = "YOGA";
  formAdd.coachId = coaches.value[0]?.id ?? null;
}

function openEdit(row) {
  formEdit.id = row.id;
  formEdit.title = row.title;
  formEdit.slot = row.slot;
  formEdit.capacity = row.capacity;
  formEdit.category = row.category || "YOGA";
  formEdit.coachId = row.coach?.id ?? null;
  formEdit.enabled = row.enabled !== false;
  dlgEdit.value = true;
}

async function saveAdd() {
  if (!formAdd.title || !formAdd.slot || !formAdd.coachId) {
    ElMessage.warning("请填写课程名称、时间与教练");
    return;
  }
  saving.value = true;
  try {
    await api.post("/courses", {
      title: formAdd.title,
      slot: formAdd.slot,
      capacity: formAdd.capacity,
      category: formAdd.category,
      enabled: true,
      coachId: formAdd.coachId
    });
    ElMessage.success("已创建");
    dlgAdd.value = false;
    await load();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || "失败");
  } finally {
    saving.value = false;
  }
}

async function saveEdit() {
  saving.value = true;
  try {
    const body = {
      title: formEdit.title,
      slot: formEdit.slot,
      capacity: formEdit.capacity,
      category: formEdit.category,
      enabled: formEdit.enabled
    };
    if (auth.canEditCourse && formEdit.coachId) body.coachId = formEdit.coachId;
    await api.put(`/manage/courses/${formEdit.id}`, body);
    ElMessage.success("已保存");
    dlgEdit.value = false;
    await load();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || "失败");
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  await loadCoaches();
  await load();
});
</script>
