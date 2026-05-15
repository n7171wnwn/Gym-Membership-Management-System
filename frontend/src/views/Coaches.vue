<template>
  <el-card class="gym-page-card">
    <template #header>
      <span>教练管理</span>
      <el-button v-if="auth.canManageCoach" type="primary" style="float: right" @click="openAdd">新增教练</el-button>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="specialty" label="擅长方向" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="auth.canManageCoach" type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button v-if="auth.canManageCoach" type="danger" link @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg" :title="editingId ? '编辑教练' : '新增教练'" width="420px" destroy-on-close @closed="reset">
      <el-form :model="form" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="擅长"><el-input v-model="form.specialty" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import api from "../api/http";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();

const list = ref([]);
const loading = ref(false);
const dlg = ref(false);
const saving = ref(false);
const editingId = ref(null);
const form = reactive({ name: "", specialty: "" });

async function load() {
  loading.value = true;
  try {
    const { data } = await api.get("/coaches");
    list.value = data;
  } finally {
    loading.value = false;
  }
}

function openAdd() {
  editingId.value = null;
  reset();
  dlg.value = true;
}

function openEdit(row) {
  editingId.value = row.id;
  form.name = row.name || "";
  form.specialty = row.specialty || "";
  dlg.value = true;
}

function reset() {
  form.name = "";
  form.specialty = "";
}

async function save() {
  if (!form.name || !form.specialty) {
    ElMessage.warning("请填写完整");
    return;
  }
  saving.value = true;
  try {
    const payload = { name: form.name, specialty: form.specialty };
    if (editingId.value) {
      await api.put(`/coaches/${editingId.value}`, payload);
      ElMessage.success("已更新");
    } else {
      await api.post("/coaches", payload);
      ElMessage.success("已保存");
    }
    dlg.value = false;
    await load();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || (editingId.value ? "更新失败" : "保存失败"));
  } finally {
    saving.value = false;
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除教练「${row.name}」？`, "删除", { type: "warning" });
    await api.delete(`/gym/coaches/${row.id}`);
    ElMessage.success("已删除");
    await load();
  } catch (e) {
    if (e !== "cancel") {
      ElMessage.error(e.response?.data?.message || "删除失败");
    }
  }
}

onMounted(load);
</script>
