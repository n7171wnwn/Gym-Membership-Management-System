const api = require('../../utils/api');
const util = require('../../utils/util');

Page({
  data: {
    announcements: [],
    hotCourses: []
  },

  onShow() {
    this.loadAnnouncements();
    this.loadHot();
  },

  async loadAnnouncements() {
    try {
      const list = await api.request('/gym/announcements', 'GET');
      this.setData({ announcements: list || [] });
    } catch (e) {
      console.error(e);
    }
  },

  async loadHot() {
    try {
      const courses = await api.request('/gym/courses', 'GET');
      const enabled = (courses || []).filter((c) => c.enabled !== false);
      const hotCourses = enabled.slice(0, 5).map((c) => ({
        ...c,
        catTxt: util.categoryLabel(c.category)
      }));
      this.setData({ hotCourses });
    } catch (e) {
      console.error(e);
    }
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/courses/detail?id=${id}` });
  }
});
