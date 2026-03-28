<template>
  <el-card class="gym-page-card">
    <template #header>
      <span>系统消息记录</span>
      <el-button style="float: right" size="small" @click="load">刷新</el-button>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="会员" width="120">
        <template #default="{ row }">{{ row.member?.name || "—" }}</template>
      </el-table-column>
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ msgTypeLabel(row.msgType) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="140" />
      <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
      <el-table-column label="已读" width="80">
        <template #default="{ row }">{{ row.readFlag ? "是" : "否" }}</template>
      </el-table-column>
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ row.createdAt?.replace("T", " ").slice(0, 19) }}</template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import api from "../api/http";
import { msgTypeLabel } from "../utils/labels";

const list = ref([]);
const loading = ref(false);

async function load() {
  loading.value = true;
  try {
    const { data } = await api.get("/manage/messages");
    list.value = data;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>
