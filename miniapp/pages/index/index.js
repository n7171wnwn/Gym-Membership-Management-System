const app = getApp();

Page({
  data: {
    list: []
  },
  onLoad() {
    this.loadAnnouncements();
  },
  loadAnnouncements() {
    wx.request({
      url: `${app.globalData.apiBase}/gym/announcements`,
      method: "GET",
      success: (res) => {
        this.setData({ list: res.data || [] });
      },
      fail: () => {
        this.setData({ list: [] });
      }
    });
  },
  goLogin() {
    wx.navigateTo({ url: "/pages/login/login" });
  }
});

