function getBase() {
  const app = getApp();
  return (app && app.globalData && app.globalData.apiBase) || 'http://localhost:8080/api';
}

function request(path, method, data) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token') || '';
    wx.request({
      url: getBase() + path,
      method: method || 'GET',
      data: data || {},
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : ''
      },
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else {
          const msg = (res.data && res.data.message) || `HTTP ${res.statusCode}`;
          reject(new Error(msg));
        }
      },
      fail(err) {
        reject(err);
      }
    });
  });
}

module.exports = { request, getBase };
