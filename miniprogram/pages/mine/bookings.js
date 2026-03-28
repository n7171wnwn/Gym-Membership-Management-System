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
      const raw = await api.request('/gym/member/bookings', 'GET');
      const list = (raw || []).map((b) => {
        const st = b.status || '';
        const stTxt = util.bookingStatusLabel(st);
        let stCls = 'st-ok';
        if (st === 'PENDING') stCls = 'st-pending';
        if (st === 'COMPLETED') stCls = 'st-off';
        if (st === 'CANCELLED') stCls = 'st-off';
        const canCancel = st === 'PENDING' || st === 'APPROVED';
        return {
          ...b,
          stTxt,
          stCls,
          canCancel,
          timeTxt: util.formatTime(b.createdAt)
        };
      });
      list.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''));
      this.setData({list});
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  },

  cancelOne(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '取消预约',
      content: '确定取消该预约？',
      success: async (r) => {
        if (!r.confirm) return;
        try {
          await api.request(`/gym/member/bookings/${id}/cancel`, 'POST', {});
          wx.showToast({ title: '已取消', icon: 'success' });
          this.load();
        } catch (err) {
          wx.showToast({ title: err.message || '失败', icon: 'none' });
        }
      }
    });
  }
});
