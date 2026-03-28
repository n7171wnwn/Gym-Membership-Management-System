const api = require('../../utils/api');
const util = require('../../utils/util');

Page({
  data: {
    course: null,
    catTxt: '',
    coachName: '',
    booking: false,
    loggedIn: false
  },

  onLoad(q) {
    this.id = q.id;
  },

  onShow() {
    this.setData({ loggedIn: !!wx.getStorageSync('token') });
    if (this.id) this.load();
  },

  async load() {
    try {
      const courses = await api.request('/gym/courses', 'GET');
      const c = (courses || []).find((x) => String(x.id) === String(this.id));
      if (!c) {
        wx.showToast({ title: '课程不存在', icon: 'none' });
        return;
      }
      this.setData({
        course: c,
        catTxt: util.categoryLabel(c.category),
        coachName: (c.coach && c.coach.name) || '—'
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  },

  goMy() {
    wx.navigateTo({ url: '/pages/mine/bookings' });
  },

  async bookOne() {
    if (!wx.getStorageSync('token')) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const mid = wx.getStorageSync('memberId');
    if (!mid) {
      wx.showToast({ title: '请重新登录', icon: 'none' });
      return;
    }
    this.setData({ booking: true });
    try {
      await api.request('/gym/bookings', 'POST', {
        memberId: Number(mid),
        courseId: Number(this.id)
      });
      wx.showToast({ title: '预约已提交，待审核', icon: 'success' });
      this.load();
    } catch (e) {
      wx.showToast({ title: e.message || '预约失败', icon: 'none' });
    } finally {
      this.setData({ booking: false });
    }
  }
});
