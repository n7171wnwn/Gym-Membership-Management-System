import { defineStore } from "pinia";
import axios from "axios";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("token") || "",
    username: localStorage.getItem("username") || "",
    role: localStorage.getItem("role") || "",
    displayName: localStorage.getItem("displayName") || ""
  }),
  getters: {
    isAdmin: (s) => s.role === "ROLE_ADMIN",
    isReception: (s) => s.role === "ROLE_RECEPTION",
    isCoach: (s) => s.role === "ROLE_COACH",
    canFullManage(state) {
      return state.role === "ROLE_ADMIN" || state.role === "ROLE_RECEPTION";
    },
    canDeleteMember(state) {
      return state.role === "ROLE_ADMIN";
    }
  },
  actions: {
    async login(username, password) {
      const res = await axios.post("/api/gym/auth/login", { username, password });
      const { token, role } = res.data;
      if (role === "ROLE_MEMBER") {
        throw new Error("管理端不支持会员账号登录");
      }
      this.token = token;
      this.username = res.data.username;
      this.role = role;
      this.displayName = res.data.displayName || res.data.username;
      localStorage.setItem("token", this.token);
      localStorage.setItem("username", this.username);
      localStorage.setItem("role", this.role);
      localStorage.setItem("displayName", this.displayName);
    },
    logout() {
      this.token = "";
      this.username = "";
      this.role = "";
      this.displayName = "";
      localStorage.removeItem("token");
      localStorage.removeItem("username");
      localStorage.removeItem("role");
      localStorage.removeItem("displayName");
    }
  }
});
