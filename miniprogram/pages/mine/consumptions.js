const api = require('../../utils/api');
const util = require('../../utils/util');

Page({
  data: { list: [] },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.load();
  },

  async load() {
    try {
      const raw = await api.request('/gym/member/consumptions', 'GET');
      const list = (raw || []).map((c) => {
        const n = c.amount != null ? Number(c.amount) : 0;
        const amtCls = n >= 0 ? 'amt-plus' : 'amt-minus';
        const sign = n >= 0 ? '+' : '';
        return {
          ...c,
          timeTxt: util.formatTime(c.createdAt),
          amtTxt: `${sign}${n.toFixed(2)} 元`,
          amtCls
        };
      });
      this.setData({ list });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  }
});
