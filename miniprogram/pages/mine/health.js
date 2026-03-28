const api = require('../../utils/api');

Page({
  data: {
    parsed: {},
    height: '',
    weight: '',
    bodyFat: '',
    note: '',
    saving: false
  },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.load();
  },

  onH(e) {
    this.setData({ height: e.detail.value });
  },
  onW(e) {
    this.setData({ weight: e.detail.value });
  },
  onF(e) {
    this.setData({ bodyFat: e.detail.value });
  },
  onNote(e) {
    this.setData({ note: e.detail.value });
  },

  async load() {
    try {
      const profile = await api.request('/gym/member/profile', 'GET');
      let parsed = {};
      const raw = profile.healthTracking;
      if (raw && typeof raw === 'string') {
        try {
          parsed = JSON.parse(raw);
        } catch (_) {
          parsed = { legacyNote: raw };
        }
      }
      this.setData({
        parsed,
        height: parsed.height != null ? String(parsed.height) : '',
        weight: parsed.weight != null ? String(parsed.weight) : '',
        bodyFat: parsed.bodyFat != null ? String(parsed.bodyFat) : '',
        note: parsed.note != null ? String(parsed.note) : ''
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  },

  async save() {
    this.setData({ saving: true });
    try {
      const body = {};
      if (this.data.height !== '') body.height = Number(this.data.height);
      if (this.data.weight !== '') body.weight = Number(this.data.weight);
      if (this.data.bodyFat !== '') body.bodyFat = Number(this.data.bodyFat);
      if (this.data.note.trim()) body.note = this.data.note.trim();
      await api.request('/gym/member/health', 'PUT', body);
      wx.showToast({ title: '已保存', icon: 'success' });
      this.load();
    } catch (e) {
      wx.showToast({ title: e.message || '保存失败', icon: 'none' });
    } finally {
      this.setData({ saving: false });
    }
  }
});
