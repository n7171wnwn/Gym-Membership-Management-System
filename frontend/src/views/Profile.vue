<template>
  <el-card class="gym-page-card" style="max-width: 480px">
    <template #header>修改密码</template>
    <el-form label-width="100px" @submit.prevent="save">
      <el-form-item label="原密码">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" @click.prevent="save">保存</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import api from "../api/http";

const loading = ref(false);
const form = reactive({ oldPassword: "", newPassword: "" });

async function save() {
  if (!form.oldPassword || !form.newPassword) {
    ElMessage.warning("请填写完整");
    return;
  }
  loading.value = true;
  try {
    await api.post("/manage/profile/password", {
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    });
    ElMessage.success("密码已更新");
    form.oldPassword = "";
    form.newPassword = "";
  } catch (e) {
    ElMessage.error(e.response?.data?.message || "修改失败");
  } finally {
    loading.value = false;
  }
}
</script>
