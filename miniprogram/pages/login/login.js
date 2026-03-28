const api = require('../../utils/api');

function saveSession(data) {
  wx.setStorageSync('token', data.token);
  if (data.memberId != null) {
    wx.setStorageSync('memberId', data.memberId);
  }
  wx.setStorageSync('displayName', data.displayName || '');
}

Page({
  data: {
    phone: '',
    username: 'member',
    password: 'member123',
    wxLoading: false,
    bindLoading: false,
    pwdLoading: false
  },

  onPhone(e) {
    this.setData({ phone: e.detail.value });
  },
  onUser(e) {
    this.setData({ username: e.detail.value });
  },
  onPass(e) {
    this.setData({ password: e.detail.value });
  },

  wxQuickLogin() {
    this.setData({ wxLoading: true });
    wx.login({
      success: async (r) => {
        if (!r.code) {
          wx.showToast({ title: '获取 code 失败', icon: 'none' });
          this.setData({ wxLoading: false });
          return;
        }
        try {
          const data = await api.request('/gym/auth/wx-login', 'POST', { code: r.code });
          if (data.needBind) {
            wx.showModal({
              title: '提示',
              content: data.message || '请先绑定手机号',
              showCancel: false
            });
          } else {
            saveSession(data);
            wx.showToast({ title: '登录成功', icon: 'success' });
            setTimeout(() => wx.switchTab({ url: '/pages/index/index' }), 500);
          }
        } catch (e) {
          wx.showToast({ title: e.message || '登录失败', icon: 'none' });
        } finally {
          this.setData({ wxLoading: false });
        }
      },
      fail: () => {
        this.setData({ wxLoading: false });
        wx.showToast({ title: 'wx.login 失败', icon: 'none' });
      }
    });
  },

  async bindPhone() {
    const phone = (this.data.phone || '').trim();
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入11位手机号', icon: 'none' });
      return;
    }
    this.setData({ bindLoading: true });
    try {
      let code = '';
      try {
        const lr = await new Promise((res, rej) => {
          wx.login({ success: res, fail: rej });
        });
        code = lr.code || '';
      } catch (_) {
        code = '';
      }
      const data = await api.request('/gym/auth/member/bind-phone', 'POST', { phone, code });
      saveSession(data);
      wx.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(() => wx.switchTab({ url: '/pages/mine/mine' }), 500);
    } catch (e) {
      wx.showToast({ title: e.message || '绑定失败', icon: 'none' });
    } finally {
      this.setData({ bindLoading: false });
    }
  },

  async pwdLogin() {
    this.setData({ pwdLoading: true });
    try {
      const data = await api.request('/gym/auth/login', 'POST', {
        username: this.data.username,
        password: this.data.password
      });
      if (data.role !== 'ROLE_MEMBER') {
        wx.showToast({ title: '请使用会员账号', icon: 'none' });
        return;
      }
      saveSession(data);
      wx.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(() => wx.switchTab({ url: '/pages/mine/mine' }), 500);
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' });
    } finally {
      this.setData({ pwdLoading: false });
    }
  }
});
