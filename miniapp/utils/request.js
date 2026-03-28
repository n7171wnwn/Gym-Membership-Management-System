const BASE_URL = "http://127.0.0.1:8080/api";

function request(url, method = "GET", data = {}) {
  const token = wx.getStorageSync("token");
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => resolve(res.data),
      fail: reject,
    });
  });
}

module.exports = { request };
