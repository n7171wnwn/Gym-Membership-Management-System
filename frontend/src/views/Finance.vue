<template>
  <div>
    <el-row :gutter="16" class="mb">
      <el-col :span="8">
        <el-card shadow="hover" class="stat">
          <div class="n">{{ finance.totalRevenue ?? "—" }}</div>
          <div class="t">累计收入（元）</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="gym-page-card mb">
      <template #header>按月汇总</template>
      <el-table :data="monthRows" size="small">
        <el-table-column prop="month" label="月份" />
        <el-table-column prop="amount" label="金额（元）" />
      </el-table>
    </el-card>

    <el-card class="gym-page-card">
      <template #header>
        <span>消费流水</span>
        <el-button style="float: right" size="small" @click="loadCon">刷新</el-button>
      </template>
      <el-table :data="consumptions" v-loading="cLoad" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="会员" width="120">
          <template #default="{ row }">{{ row.member?.name || "—" }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ row.createdAt?.replace("T", " ").slice(0, 19) || "—" }}</template>
        </el-table-column>
      </el-table>
      <div class="add-row">
        <el-divider>登记消费</el-divider>
        <el-form :inline="true" :model="form">
          <el-form-item label="会员">
            <el-select v-model="form.memberId" filterable placeholder="选择" style="width: 160px">
              <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="form.type" style="width: 120px">
              <el-option label="办卡" value="办卡" />
              <el-option label="续费" value="续费" />
              <el-option label="私教" value="私教" />
              <el-option label="买课" value="买课" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="金额">
            <el-input-number v-model="form.amount" :min="0" :precision="2" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="adding" @click="addCon">添加</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import api from "../api/http";

const finance = ref({ totalRevenue: 0, monthly: [] });
const consumptions = ref([]);
const members = ref([]);
const cLoad = ref(false);
const adding = ref(false);
const form = reactive({ memberId: null, type: "办卡", amount: 0 });

const monthRows = computed(() => {
  const m = finance.value.monthly;
  if (!m || !Array.isArray(m)) return [];
  return m.map((row) => ({
    month: String(row[0]),
    amount: row[1]
  }));
});

async function loadFinance() {
  const { data } = await api.get("/manage/stats/finance");
  finance.value = data;
}

async function loadCon() {
  cLoad.value = true;
  try {
    const { data } = await api.get("/consumptions");
    consumptions.value = data;
  } finally {
    cLoad.value = false;
  }
}

async function loadMembers() {
  const { data } = await api.get("/members");
  members.value = data;
}

async function addCon() {
  if (!form.memberId) {
    ElMessage.warning("请选择会员");
    return;
  }
  adding.value = true;
  try {
    await api.post("/consumptions", {
      memberId: form.memberId,
      type: form.type,
      amount: form.amount
    });
    ElMessage.success("已记录");
    await loadCon();
    await loadFinance();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || "失败");
  } finally {
    adding.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadFinance(), loadCon(), loadMembers()]);
});
</script>

<style scoped>
.mb {
  margin-bottom: 16px;
}
.stat {
  border-radius: 10px;
  text-align: center;
}
.stat .n {
  font-size: 28px;
  font-weight: 700;
  color: #0c4a6e;
}
.stat .t {
  color: #64748b;
  margin-top: 6px;
}
.add-row {
  margin-top: 8px;
}
</style>
