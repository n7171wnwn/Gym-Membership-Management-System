const { request } = require("../../utils/request");

Page({
  data: {
    username: "member",
    password: "member123"
  },
  onUsernameInput(e) { this.setData({ username: e.detail.value }); },
  onPasswordInput(e) { this.setData({ password: e.detail.value }); },
  async login() {
    try {
      const res = await request("/gym/auth/login", "POST", {
        username: this.data.username,
        password: this.data.password,
      });
      wx.setStorageSync("token", res.token);
      wx.navigateTo({ url: "/pages/index/index" });
    } catch (e) {
      wx.showToast({ title: "登录失败", icon: "none" });
    }
  }
});
