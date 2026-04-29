const app = getApp();

Page({
  data: {
    mode: "password",
    username: "",
    password: "",
    phone: "",
    loading: false,
    token: ""
  },
  setModePassword() {
    this.setData({ mode: "password" });
  },
  setModePhone() {
    this.setData({ mode: "phone" });
  },
  onUsername(e) {
    this.setData({ username: (e.detail && e.detail.value) ? e.detail.value.trim() : "" });
  },
  onPassword(e) {
    this.setData({ password: (e.detail && e.detail.value) ? e.detail.value : "" });
  },
  onPhone(e) {
    this.setData({ phone: (e.detail && e.detail.value) ? e.detail.value.trim() : "" });
  },
  loginByPassword() {
    const username = this.data.username;
    const password = this.data.password;
    if (!username || !password) {
      wx.showToast({ title: "请输入用户名和密码", icon: "none" });
      return;
    }
    this.setData({ loading: true });
    wx.request({
      url: `${app.globalData.apiBase}/gym/auth/login`,
      method: "POST",
      data: { username, password },
      header: { "content-type": "application/json" },
      success: (res) => {
        const token = res.data && res.data.token;
        if (!token) {
          wx.showToast({ title: res.data?.message || "登录失败", icon: "none" });
          return;
        }
        app.globalData.token = token;
        this.setData({ token });
        wx.showToast({ title: "登录成功", icon: "success" });
      },
      fail: (e) => {
        wx.showToast({ title: e?.errMsg || "网络错误", icon: "none" });
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },
  bindPhone() {
    const phone = this.data.phone;
    if (!phone) {
      wx.showToast({ title: "请输入手机号", icon: "none" });
      return;
    }
    this.setData({ loading: true });
    wx.request({
      url: `${app.globalData.apiBase}/gym/auth/member/bind-phone`,
      method: "POST",
      data: { phone },
      header: { "content-type": "application/json" },
      success: (res) => {
        const token = res.data && res.data.token;
        if (!token) {
          wx.showToast({ title: res.data?.message || "登录失败", icon: "none" });
          return;
        }
        app.globalData.token = token;
        this.setData({ token });
        wx.showToast({ title: "登录成功", icon: "success" });
      },
      fail: (e) => {
        wx.showToast({ title: e?.errMsg || "网络错误", icon: "none" });
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  }
});

