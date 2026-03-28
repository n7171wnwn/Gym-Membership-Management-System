const api = require('../../utils/api');

Page({
  data: {
    logged: false,
    profile: {},
    nameInitial: '?'
  },

  onShow() {
    const logged = !!wx.getStorageSync('token');
    this.setData({ logged });
    if (logged) {
      this.loadProfile();
    } else {
      this.setData({ profile: {}, nameInitial: '?' });
    }
  },

  async loadProfile() {
    try {
      const profile = await api.request('/gym/member/profile', 'GET');
      const initial = (profile.name && profile.name.charAt(0)) || '?';
      this.setData({ profile, nameInitial: initial });
    } catch (e) {
      if (e.message && e.message.indexOf('401') >= 0) {
        wx.removeStorageSync('token');
        this.setData({ logged: false });
      }
    }
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' });
  },

  logout() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('memberId');
    wx.removeStorageSync('displayName');
    this.setData({ logged: false, profile: {}, nameInitial: '?' });
    wx.showToast({ title: '已退出', icon: 'none' });
  }
});
