<template>
  <el-card class="gym-page-card">
    <template #header>
      <span>教练管理</span>
      <el-button type="primary" style="float: right" @click="openAdd">新增教练</el-button>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="specialty" label="擅长方向" />
    </el-table>

    <el-dialog v-model="dlg" title="新增教练" width="420px" destroy-on-close @closed="reset">
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
import { ElMessage } from "element-plus";
import api from "../api/http";

const list = ref([]);
const loading = ref(false);
const dlg = ref(false);
const saving = ref(false);
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
  reset();
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
    await api.post("/coaches", { name: form.name, specialty: form.specialty });
    ElMessage.success("已保存");
    dlg.value = false;
    await load();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || "保存失败");
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>
