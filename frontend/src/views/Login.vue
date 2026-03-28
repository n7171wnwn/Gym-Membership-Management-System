<template>
  <div class="login-wrap">
    <div class="panel">
      <h1>健身房管理后台</h1>
      <p class="sub">账号密码登录（管理员 / 前台 / 教练）</p>
      <el-form :model="form" @submit.prevent="submit" label-position="top">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="如 admin" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-button type="primary" class="btn" native-type="submit" :loading="loading" @click.prevent="submit">
          登录
        </el-button>
      </el-form>
      <p class="hint">测试账号：admin/admin123、reception/reception123、coach/coach123</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();
const loading = ref(false);
const form = reactive({ username: "admin", password: "admin123" });

async function submit() {
  loading.value = true;
  try {
    await auth.login(form.username, form.password);
    const redirect = route.query.redirect || "/dashboard";
    router.replace(typeof redirect === "string" ? redirect : "/dashboard");
    ElMessage.success("登录成功");
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || "登录失败");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0c4a6e 0%, #0d9488 45%, #155e75 100%);
}
.panel {
  width: 400px;
  padding: 36px 40px 28px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
}
h1 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #0c4a6e;
}
.sub {
  margin: 0 0 24px;
  color: #64748b;
  font-size: 14px;
}
.btn {
  width: 100%;
  margin-top: 8px;
}
.hint {
  margin: 16px 0 0;
  font-size: 12px;
  color: #94a3b8;
}
</style>
