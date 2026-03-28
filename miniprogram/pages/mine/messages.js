const api = require('../../utils/api');
const util = require('../../utils/util');

function msgTypeTxt(t) {
  if (t === 'BOOKING_OK') return '课程预约';
  if (t === 'BOOKING_CANCEL') return '预约取消';
  if (t === 'EXPIRE') return '会员到期';
  return t || '通知';
}

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
      const raw = await api.request('/gym/member/messages', 'GET');
      const list = (raw || []).map((m) => ({
        ...m,
        typeTxt: msgTypeTxt(m.msgType),
        timeTxt: util.formatTime(m.createdAt)
      }));
      list.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''));
      this.setData({ list });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  }
});
