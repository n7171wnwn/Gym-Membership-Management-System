const { request } = require("../../utils/request");

Page({
  data: {
    dashboard: {},
    courses: []
  },
  async onShow() {
    try {
      const dashboard = await request("/gym/dashboard");
      const courses = await request("/gym/courses");
      this.setData({ dashboard, courses });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
    }
  },
  async book(e) {
    const courseId = e.currentTarget.dataset.id;
    try {
      await request("/gym/bookings", "POST", { memberId: 1, courseId });
      wx.showToast({ title: "预约成功", icon: "success" });
      this.onShow();
    } catch (e2) {
      wx.showToast({ title: (e2 && e2.message) || "预约失败", icon: "none" });
    }
  }
});
