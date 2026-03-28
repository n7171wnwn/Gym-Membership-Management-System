<template>
  <div>
    <el-card class="gym-page-card">
      <el-form :inline="true" class="toolbar">
        <el-form-item label="搜索">
          <el-input v-model="keyword" clearable placeholder="姓名/手机" style="width: 200px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="status" clearable placeholder="全部" style="width: 120px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="过期" value="EXPIRED" />
            <el-option label="冻结" value="FROZEN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="openCreate">新增会员</el-button>
          <el-button @click="exportXlsx">导出 Excel</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机" width="120" />
        <el-table-column prop="goal" label="目标" show-overflow-tooltip min-width="120" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ memberStatusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="expireDate" label="到期" width="110" />
        <el-table-column prop="level" label="等级" width="80" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="openCards(row)">会员卡</el-button>
            <el-button v-if="auth.canDeleteMember" type="danger" link @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlgMember" :title="editId ? '编辑会员' : '新增会员'" width="520px" destroy-on-close @closed="resetMember">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="目标"><el-input v-model="form.goal" /></el-form-item>
        <el-form-item label="到期日"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="过期" value="EXPIRED" />
            <el-option label="冻结" value="FROZEN" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级"><el-input v-model="form.level" placeholder="如 Bronze" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgMember = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveMember">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dlgCards" title="会员卡" width="640px" destroy-on-close @open="loadCards">
      <template v-if="cardMember">
        <p class="who">会员：{{ cardMember.name }}（{{ cardMember.phone }}）</p>
        <el-table :data="cards" size="small" v-loading="cardLoading">
          <el-table-column label="类型" width="90">
            <template #default="{ row }">{{ cardTypeLabel(row.cardType) }}</template>
          </el-table-column>
          <el-table-column label="余额" width="80"><template #default="{ row }">{{ row.balance ?? 0 }}</template></el-table-column>
          <el-table-column label="剩余次数" width="90"><template #default="{ row }">{{ row.remainingTimes ?? "—" }}</template></el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">{{ cardStatusLabel(row.status) }}</template>
          </el-table-column>
          <el-table-column label="有效期" width="200">
            <template #default="{ row }">{{ row.validFrom }} ~ {{ row.validTo }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button v-if="row.status === 'ACTIVE'" type="primary" link @click="openRenew(row)">续费</el-button>
              <el-button v-if="row.status === 'ACTIVE'" type="warning" link @click="doLost(row)">挂失</el-button>
              <el-button v-if="row.status === 'ACTIVE'" type="danger" link @click="doCancelCard(row)">注销</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-divider>办卡</el-divider>
        <el-form :inline="true" :model="issueForm">
          <el-form-item label="卡类型">
            <el-select v-model="issueForm.cardType" style="width: 120px">
              <el-option label="月卡" value="MONTH" />
              <el-option label="季卡" value="SEASON" />
              <el-option label="年卡" value="YEAR" />
              <el-option label="次卡" value="COUNT" />
            </el-select>
          </el-form-item>
          <el-form-item label="实付(元)">
            <el-input-number v-model="issueForm.payAmount" :min="0" :precision="2" />
          </el-form-item>
          <el-form-item label="有效天数">
            <el-input-number v-model="issueForm.validDays" :min="1" />
          </el-form-item>
          <el-form-item v-if="issueForm.cardType === 'COUNT'" label="次数">
            <el-input-number v-model="issueForm.remainingTimes" :min="0" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="issueCard">办卡</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-dialog>

    <el-dialog v-model="dlgRenew" title="续费" width="400px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="金额(元)"><el-input-number v-model="renewForm.payAmount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="延长(天)"><el-input-number v-model="renewForm.extendDays" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgRenew = false">取消</el-button>
        <el-button type="primary" @click="submitRenew">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import api from "../api/http";
import { useAuthStore } from "../stores/auth";
import { memberStatusLabel, cardTypeLabel, cardStatusLabel } from "../utils/labels";

const auth = useAuthStore();
const list = ref([]);
const loading = ref(false);
const keyword = ref("");
const status = ref("");

const dlgMember = ref(false);
const editId = ref(null);
const saving = ref(false);
const form = reactive({
  name: "",
  phone: "",
  goal: "",
  expireDate: "",
  status: "NORMAL",
  level: "Bronze"
});

const dlgCards = ref(false);
const cardMember = ref(null);
const cards = ref([]);
const cardLoading = ref(false);
const issueForm = reactive({
  cardType: "MONTH",
  payAmount: 0,
  validDays: 30,
  remainingTimes: 10
});

const dlgRenew = ref(false);
const renewTarget = ref(null);
const renewForm = reactive({ payAmount: 0, extendDays: 30 });

async function load() {
  loading.value = true;
  try {
    const params = {};
    if (keyword.value) params.keyword = keyword.value;
    if (status.value) params.status = status.value;
    const { data } = await api.get("/manage/members", { params });
    list.value = data;
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editId.value = null;
  resetMember();
  dlgMember.value = true;
}

function openEdit(row) {
  editId.value = row.id;
  form.name = row.name;
  form.phone = row.phone;
  form.goal = row.goal || "";
  form.expireDate = row.expireDate || "";
  form.status = row.status || "NORMAL";
  form.level = row.level || "Bronze";
  dlgMember.value = true;
}

function resetMember() {
  form.name = "";
  form.phone = "";
  form.goal = "";
  form.expireDate = "";
  form.status = "NORMAL";
  form.level = "Bronze";
}

async function saveMember() {
  if (!form.name || !form.phone) {
    ElMessage.warning("请填写姓名与手机");
    return;
  }
  saving.value = true;
  try {
    if (editId.value) {
      await api.put(`/manage/members/${editId.value}`, { ...form });
    } else {
      await api.post("/manage/members", { ...form });
    }
    ElMessage.success("已保存");
    dlgMember.value = false;
    await load();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || "失败");
  } finally {
    saving.value = false;
  }
}

async function del(row) {
  try {
    await ElMessageBox.confirm(`确定删除会员「${row.name}」？`, "删除", { type: "warning" });
    await api.delete(`/manage/members/${row.id}`);
    ElMessage.success("已删除");
    await load();
  } catch (e) {
    if (e !== "cancel") ElMessage.error(e.response?.data?.message || "删除失败");
  }
}

function openCards(row) {
  cardMember.value = row;
  dlgCards.value = true;
}

async function loadCards() {
  if (!cardMember.value) return;
  cardLoading.value = true;
  try {
    const { data } = await api.get(`/manage/members/${cardMember.value.id}/cards`);
    cards.value = data;
  } finally {
    cardLoading.value = false;
  }
}

async function issueCard() {
  try {
    await api.post("/manage/cards/issue", {
      memberId: cardMember.value.id,
      cardType: issueForm.cardType,
      payAmount: issueForm.payAmount,
      validDays: issueForm.validDays,
      remainingTimes: issueForm.cardType === "COUNT" ? issueForm.remainingTimes : undefined
    });
    ElMessage.success("办卡成功");
    await loadCards();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || "失败");
  }
}

function openRenew(row) {
  renewTarget.value = row;
  renewForm.payAmount = 0;
  renewForm.extendDays = 30;
  dlgRenew.value = true;
}

async function submitRenew() {
  try {
    await api.post(`/manage/cards/${renewTarget.value.id}/renew`, {
      payAmount: renewForm.payAmount,
      extendDays: renewForm.extendDays
    });
    ElMessage.success("续费成功");
    dlgRenew.value = false;
    await loadCards();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || "失败");
  }
}

async function doLost(row) {
  try {
    await ElMessageBox.confirm("确认挂失？该卡将不可用。", "挂失");
    await api.post(`/manage/cards/${row.id}/lost`);
    ElMessage.success("已挂失");
    await loadCards();
  } catch (e) {
    if (e !== "cancel") ElMessage.error(e.response?.data?.message || " fail");
  }
}

async function doCancelCard(row) {
  try {
    await ElMessageBox.confirm("确认注销该会员卡？", "注销", { type: "warning" });
    await api.post(`/manage/cards/${row.id}/cancel`);
    ElMessage.success("已注销");
    await loadCards();
  } catch (e) {
    if (e !== "cancel") ElMessage.error(e.response?.data?.message || "失败");
  }
}

async function exportXlsx() {
  try {
    const res = await api.get("/members/export", { responseType: "blob" });
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const a = document.createElement("a");
    a.href = url;
    a.download = "member-report.xlsx";
    a.click();
    window.URL.revokeObjectURL(url);
  } catch {
    ElMessage.error("导出失败");
  }
}

onMounted(load);
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.who {
  margin: 0 0 12px;
  font-weight: 600;
  color: #0c4a6e;
}
</style>
