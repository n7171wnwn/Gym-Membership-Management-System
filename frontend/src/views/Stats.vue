<template>
  <div>
    <el-row :gutter="16" class="mb">
      <el-col :span="6">
        <el-card shadow="hover"><div class="k">会员数</div><div class="v">{{ charts.memberCount ?? "—" }}</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="k">课程数</div><div class="v">{{ charts.courseCount ?? "—" }}</div></el-card>
      </el-col>
    </el-row>
    <el-card class="gym-page-card mb">
      <template #header>营收趋势</template>
      <div ref="revRef" class="chart"></div>
    </el-card>
    <el-card class="gym-page-card">
      <template #header>课程预约热度</template>
      <div ref="heatRef" class="chart"></div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick } from "vue";
import * as echarts from "echarts";
import api from "../api/http";

const charts = ref({});
const revRef = ref(null);
const heatRef = ref(null);
let revChart;
let heatChart;

async function load() {
  const { data } = await api.get("/manage/stats/charts");
  charts.value = data;
  await nextTick();
  renderRev(data);
  renderHeat(data.bookingHeat || []);
}

function renderRev(data) {
  const months = data.revenueMonths || [];
  const amounts = data.revenueAmounts || [];
  if (!revChart && revRef.value) revChart = echarts.init(revRef.value);
  if (!revChart) return;
  revChart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["营收(元)"] },
    grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
    xAxis: { type: "category", data: months, boundaryGap: true },
    yAxis: { type: "value" },
    series: [
      {
        name: "营收(元)",
        type: "bar",
        data: amounts,
        itemStyle: { color: "#0e7490" }
      },
      {
        name: "趋势",
        type: "line",
        smooth: true,
        data: amounts,
        itemStyle: { color: "#f59e0b" }
      }
    ]
  });
}

function renderHeat(rows) {
  if (!heatChart && heatRef.value) heatChart = echarts.init(heatRef.value);
  if (!heatChart) return;
  const titles = rows.map((r) => r.courseTitle || r.title || "课程");
  const counts = rows.map((r) => r.count || 0);
  heatChart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: titles.length > 6 ? "12%" : "3%", containLabel: true },
    xAxis: { type: "category", data: titles, axisLabel: { rotate: 30 } },
    yAxis: { type: "value" },
    series: [{ type: "bar", data: counts, itemStyle: { color: "#0891b2" } }]
  });
}

onMounted(async () => {
  await load();
  window.addEventListener("resize", () => {
    revChart?.resize();
    heatChart?.resize();
  });
});
</script>

<style scoped>
.mb {
  margin-bottom: 16px;
}
.chart {
  height: 360px;
}
.k {
  font-size: 13px;
  color: #64748b;
}
.v {
  font-size: 22px;
  font-weight: 700;
  color: #0c4a6e;
  margin-top: 4px;
}
</style>
