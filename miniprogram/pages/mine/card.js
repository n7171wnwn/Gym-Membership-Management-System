const api = require('../../utils/api');
const util = require('../../utils/util');

function cardTypeTxt(t) {
  if (t === 'MONTH') return '月卡';
  if (t === 'SEASON') return '季卡';
  if (t === 'YEAR') return '年卡';
  if (t === 'COUNT') return '次卡';
  return t || '会员卡';
}

function cardStatusTxt(s) {
  if (s === 'ACTIVE') return '在用';
  if (s === 'LOST') return '挂失';
  if (s === 'CANCELLED') return '已作废';
  return s || '—';
}

Page({
  data: { cards: [] },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.load();
  },

  async load() {
    try {
      const raw = await api.request('/gym/member/cards', 'GET');
      const cards = (raw || []).map((c) => ({
        ...c,
        typeTxt: cardTypeTxt(c.cardType),
        statusTxt: cardStatusTxt(c.status),
        validFrom: util.formatTime(c.validFrom),
        validTo: util.formatTime(c.validTo)
      }));
      this.setData({ cards });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  }
});
