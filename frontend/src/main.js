import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus, { ElMessage } from "element-plus";
import zhCn from "element-plus/dist/locale/zh-cn.mjs";
import "element-plus/dist/index.css";
import App from "./App.vue";
import router from "./router";
import { useAuthStore } from "./stores/auth";
import api, { setTokenGetter } from "./api/http";
import "./styles/theme.css";

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
app.use(router);
app.use(ElementPlus, { locale: zhCn });

const auth = useAuthStore();
setTokenGetter(() => auth.token);

api.interceptors.response.use(
  (r) => r,
  (err) => {
    const s = err.response?.status;
    if (s === 401 || s === 403) {
      ElMessage.error(s === 403 ? "没有权限访问该功能" : "登录已过期或无效，请重新登录");
      if (s === 401) {
        auth.logout();
        router.push("/login");
      }
    }
    return Promise.reject(err);
  }
);

app.mount("#app");
