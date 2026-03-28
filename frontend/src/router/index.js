import { createRouter, createWebHistory } from "vue-router";
import { ElMessage } from "element-plus";
import { useAuthStore } from "../stores/auth";

const routes = [
  {
    path: "/login",
    name: "login",
    component: () => import("../views/Login.vue"),
    meta: { public: true }
  },
  {
    path: "/",
    component: () => import("../layout/AdminLayout.vue"),
    meta: { requiresAuth: true },
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "dashboard",
        component: () => import("../views/Dashboard.vue")
      },
      {
        path: "members",
        name: "members",
        component: () => import("../views/Members.vue"),
        meta: { roles: ["ROLE_ADMIN", "ROLE_RECEPTION"] }
      },
      {
        path: "courses",
        name: "courses",
        component: () => import("../views/Courses.vue")
      },
      {
        path: "bookings",
        name: "bookings",
        component: () => import("../views/Bookings.vue")
      },
      {
        path: "coaches",
        name: "coaches",
        component: () => import("../views/Coaches.vue"),
        meta: { roles: ["ROLE_ADMIN", "ROLE_RECEPTION"] }
      },
      {
        path: "finance",
        name: "finance",
        component: () => import("../views/Finance.vue"),
        meta: { roles: ["ROLE_ADMIN", "ROLE_RECEPTION"] }
      },
      {
        path: "stats",
        name: "stats",
        component: () => import("../views/Stats.vue"),
        meta: { roles: ["ROLE_ADMIN", "ROLE_RECEPTION"] }
      },
      {
        path: "messages",
        name: "messages",
        component: () => import("../views/Messages.vue")
      },
      {
        path: "profile",
        name: "profile",
        component: () => import("../views/Profile.vue")
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore();
  if (to.meta.public) {
    if (auth.token && to.path === "/login") return next("/dashboard");
    return next();
  }
  if (!auth.token) {
    return next({ path: "/login", query: { redirect: to.fullPath } });
  }
  if (to.meta.roles && !to.meta.roles.includes(auth.role)) {
    ElMessage.warning("当前角色无权访问该页面");
    return next("/dashboard");
  }
  next();
});

export default router;
