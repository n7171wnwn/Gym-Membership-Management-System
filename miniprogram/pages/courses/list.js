const api = require('../../utils/api');
const util = require('../../utils/util');

Page({
  data: {
    raw: [],
    filtered: [],
    catOptions: [
      { label: '全部分类', value: '' },
      { label: '瑜伽', value: 'YOGA' },
      { label: '有氧', value: 'AEROBIC' },
      { label: '力量', value: 'STRENGTH' },
      { label: '私教', value: 'PRIVATE' }
    ],
    catIndex: 0,
    coachKw: ''
  },

  onShow() {
    this.load();
  },

  async load() {
    try {
      const courses = await api.request('/gym/courses', 'GET');
      const raw = (courses || [])
        .filter((c) => c.enabled !== false)
        .map((c) => ({
          ...c,
          catTxt: util.categoryLabel(c.category),
          coachName: (c.coach && c.coach.name) || '—'
        }));
      this.setData({ raw });
      this.applyFilter();
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  },

  onCat(e) {
    this.setData({ catIndex: Number(e.detail.value) });
    this.applyFilter();
  },

  onCoachInput(e) {
    this.setData({ coachKw: e.detail.value });
    this.applyFilter();
  },

  applyFilter() {
    const { raw, catOptions, catIndex, coachKw } = this.data;
    const catVal = catOptions[catIndex].value;
    const kw = (coachKw || '').trim();
    let list = raw.slice();
    if (catVal) {
      list = list.filter((c) => c.category === catVal);
    }
    if (kw) {
      list = list.filter((c) => c.coachName && c.coachName.indexOf(kw) >= 0);
    }
    list.sort((a, b) => String(a.slot).localeCompare(String(b.slot), 'zh'));
    this.setData({ filtered: list });
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/courses/detail?id=${id}` });
  }
});
