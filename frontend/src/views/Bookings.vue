<template>
  <el-card class="gym-page-card">
    <template #header>
      <span>预约管理</span>
    </template>

    <el-form :inline="true" class="filters">
      <el-form-item v-if="auth.canFullManage" label="会员">
        <el-select v-model="q.memberId" clearable filterable placeholder="全部" style="width: 160px">
          <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-else label="会员ID">
        <el-input v-model.number="q.memberIdManual" clearable placeholder="可选填数字" style="width: 140px" />
      </el-form-item>
      <el-form-item label="课程">
        <el-select v-model="q.courseId" clearable filterable placeholder="全部" style="width: 200px">
          <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="q.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item label="从">
        <el-date-picker v-model="q.from" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="开始" />
      </el-form-item>
      <el-form-item label="到">
        <el-date-picker v-model="q.to" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="结束" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
      <el-form-item v-if="auth.canFullManage">
        <el-button @click="openNew">代客预约</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      v-if="conflictHint"
      type="warning"
      :closable="false"
      class="mb"
      :title="conflictHint"
    />

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="会员" width="120">
        <template #default="{ row }">{{ row.member?.name }}</template>
      </el-table-column>
      <el-table-column label="课程" min-width="140">
        <template #default="{ row }">{{ row.course?.title }}</template>
      </el-table-column>
      <el-table-column label="剩余名额" width="100">
        <template #default="{ row }">{{ row.course?.remainingSlots ?? "—" }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ bookingStatusLabel(row.status) }}</template>
      </el-table-column>
      <el-table-column label="申请时间" width="170">
        <template #default="{ row }">{{ row.createdAt?.replace("T", " ").slice(0, 19) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button type="success" link @click="approve(row)">通过</el-button>
            <el-button type="danger" link @click="cancel(row)">取消</el-button>
          </template>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg" title="代客预约" width="420px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="会员">
          <el-select v-model="newBk.memberId" filterable style="width: 100%">
            <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="newBk.courseId" filterable style="width: 100%">
            <el-option v-for="c in courses" :key="c.id" :label="`${c.title}（余${c.remainingSlots}）`" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">关闭</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBk">提交</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import api from "../api/http";
import { useAuthStore } from "../stores/auth";
import { bookingStatusLabel } from "../utils/labels";

const auth = useAuthStore();
const list = ref([]);
const members = ref([]);
const courses = ref([]);
const loading = ref(false);
const dlg = ref(false);
const submitting = ref(false);
const q = reactive({
  memberId: null,
  memberIdManual: null,
  courseId: null,
  status: null,
  from: null,
  to: null
});
const newBk = reactive({ memberId: null, courseId: null });

const conflictHint = computed(() => {
  const pending = list.value.filter((b) => b.status === "PENDING");
  const full = pending.filter((b) => (b.course?.remainingSlots ?? 0) <= 0);
  if (!full.length) return "";
  return `有 ${full.length} 条待审核预约对应课程名额已满，审核通过将失败，请先扩容或拒绝。`;
});

async function load() {
  loading.value = true;
  try {
    const params = {};
    if (auth.canFullManage && q.memberId) params.memberId = q.memberId;
    if (!auth.canFullManage && q.memberIdManual) params.memberId = q.memberIdManual;
    if (q.courseId) params.courseId = q.courseId;
    if (q.status) params.status = q.status;
    if (q.from) params.fromTime = q.from;
    if (q.to) params.toTime = q.to;
    const { data } = await api.get("/manage/bookings", { params });
    list.value = data;
  } finally {
    loading.value = false;
  }
}

async function loadRefs() {
  try {
    if (auth.canFullManage) {
      const [m, c] = await Promise.all([api.get("/members"), api.get("/courses")]);
      members.value = m.data;
      courses.value = c.data;
    } else {
      const { data } = await api.get("/manage/coach/courses");
      courses.value = data;
      members.value = [];
    }
  } catch {
    members.value = [];
    courses.value = [];
  }
}

function openNew() {
  newBk.memberId = members.value[0]?.id ?? null;
  newBk.courseId = courses.value[0]?.id ?? null;
  dlg.value = true;
}

async function submitBk() {
  if (!newBk.memberId || !newBk.courseId) {
    ElMessage.warning("请选择会员与课程");
    return;
  }
  submitting.value = true;
  try {
    await api.post("/bookings", { memberId: newBk.memberId, courseId: newBk.courseId });
    ElMessage.success("已提交预约，待审核");
    dlg.value = false;
    await load();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || "失败");
  } finally {
    submitting.value = false;
  }
}

async function approve(row) {
  try {
    await ElMessageBox.confirm("确认通过该预约？通过后将占用课程名额。", "审核");
    await api.post(`/manage/bookings/${row.id}/approve`);
    ElMessage.success("已通过");
    await load();
    await loadRefs();
  } catch (e) {
    if (e !== "cancel") {
      ElMessage.error(e.response?.data?.message || "操作失败");
    }
  }
}

async function cancel(row) {
  try {
    await ElMessageBox.confirm("确认取消该预约？", "取消");
    await api.post(`/manage/bookings/${row.id}/cancel`);
    ElMessage.success("已取消");
    await load();
    await loadRefs();
  } catch (e) {
    if (e !== "cancel") {
      ElMessage.error(e.response?.data?.message || "操作失败");
    }
  }
}

onMounted(async () => {
  await loadRefs();
  await load();
});
</script>

<style scoped>
.filters {
  margin-bottom: 12px;
}
.mb {
  margin-bottom: 12px;
}
.muted {
  color: #94a3b8;
}
</style>
