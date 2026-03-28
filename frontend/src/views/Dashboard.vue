<template>
  <div>
    <el-row :gutter="16" class="mb">
      <el-col v-for="item in statItems" :key="item.key" :span="6" :xs="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-val">{{ item.val }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="gym-page-card">
      <template #header>
        <span>快捷入口</span>
      </template>
      <el-space wrap>
        <el-button v-if="auth.canFullManage" type="primary" @click="$router.push('/members')">会员管理</el-button>
        <el-button type="primary" plain @click="$router.push('/courses')">课程</el-button>
        <el-button type="primary" plain @click="$router.push('/bookings')">预约审核</el-button>
        <el-button v-if="auth.canFullManage" @click="$router.push('/finance')">财务</el-button>
        <el-button v-if="auth.canFullManage" @click="$router.push('/stats')">统计图表</el-button>
        <el-button @click="$router.push('/messages')">系统消息</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import api from "../api/http";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const dash = ref({});

onMounted(async () => {
  try {
    const { data } = await api.get("/dashboard");
    dash.value = data;
  } catch (_) {
    dash.value = {};
  }
});

const statItems = computed(() => {
  const d = dash.value;
  return [
    { key: "m", label: "会员数", val: d.memberCount ?? "—" },
    { key: "c", label: "课程数", val: d.courseCount ?? "—" },
    { key: "co", label: "教练数", val: d.coachCount ?? "—" },
    { key: "r", label: "总营收(元)", val: d.totalRevenue ?? "—" },
    { key: "e", label: "即将到期", val: d.expiringSoon ?? "—" },
    { key: "s", label: "同步日志条数", val: d.syncLogCount ?? "—" }
  ];
});
</script>

<style scoped>
.mb {
  margin-bottom: 16px;
}
.stat-card {
  margin-bottom: 16px;
  border-radius: 10px;
  border: none;
}
.stat-val {
  font-size: 26px;
  font-weight: 700;
  color: #0c4a6e;
}
.stat-label {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}
</style>
