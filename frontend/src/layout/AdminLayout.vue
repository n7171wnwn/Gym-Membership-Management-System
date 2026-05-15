<template>
  <el-container class="layout-root">
    <el-aside width="220px" class="aside">
      <div class="brand">健身房管理后台</div>
      <el-menu
        :default-active="active"
        router
        background-color="var(--gym-sidebar)"
        text-color="var(--gym-sidebar-text)"
        active-text-color="#fff"
        class="side-menu"
      >
        <el-menu-item index="/dashboard">
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item v-if="auth.canFullManage" index="/members">
          <span>会员管理</span>
        </el-menu-item>
        <el-menu-item index="/courses">
          <span>{{ auth.isCoach ? "我的课程" : "课程管理" }}</span>
        </el-menu-item>
        <el-menu-item index="/bookings">
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item v-if="auth.canManageCoach" index="/coaches">
          <span>教练管理</span>
        </el-menu-item>
        <el-menu-item v-if="auth.canFullManage" index="/finance">
          <span>消费与财务</span>
        </el-menu-item>
        <el-menu-item v-if="auth.canFullManage" index="/stats">
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/messages">
          <span>系统消息</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="breadcrumb">{{ title }}</span>
        <div class="header-right">
          <span class="who">{{ auth.displayName || auth.username }}</span>
          <el-tag size="small" effect="dark" type="info">{{ roleZh }}</el-tag>
          <el-button type="primary" link @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();

const active = computed(() => route.path);

const title = computed(() => {
  const map = {
    "/dashboard": "控制台",
    "/members": "会员管理",
    "/courses": auth.isCoach ? "我的课程" : "课程管理",
    "/bookings": "预约管理",
    "/coaches": "教练管理",
    "/finance": "消费与财务",
    "/stats": "数据统计",
    "/messages": "系统消息",
    "/profile": "个人中心"
  };
  return map[route.path] || "管理后台";
});

const roleZh = computed(() => {
  if (auth.isAdmin) return "管理员";
  if (auth.isReception) return "前台";
  if (auth.isCoach) return "教练";
  return auth.role || "";
});

function onLogout() {
  auth.logout();
  router.push("/login");
}
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
}
.aside {
  background: var(--gym-sidebar);
  border-right: 1px solid #0a3a56;
}
.brand {
  padding: 20px 16px;
  font-weight: 700;
  color: #fff;
  font-size: 15px;
  letter-spacing: 0.02em;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.side-menu {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.06);
  padding: 0 20px;
}
.breadcrumb {
  font-size: 16px;
  font-weight: 600;
  color: #0c4a6e;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.who {
  color: #64748b;
  font-size: 14px;
}
.main {
  background: var(--gym-bg);
  min-height: calc(100vh - 60px);
}
</style>
