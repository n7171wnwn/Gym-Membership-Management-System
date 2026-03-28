export const memberStatusLabel = (s) =>
  ({ NORMAL: "正常", EXPIRED: "过期", FROZEN: "冻结" }[s] || s || "—");

export const bookingStatusLabel = (s) =>
  ({ PENDING: "待审核", APPROVED: "已通过", CANCELLED: "已取消" }[s] || s || "—");

export const cardTypeLabel = (t) =>
  ({ MONTH: "月卡", SEASON: "季卡", YEAR: "年卡", COUNT: "次卡" }[t] || t || "—");

export const cardStatusLabel = (s) =>
  ({ ACTIVE: "有效", LOST: "挂失", CANCELLED: "已注销" }[s] || s || "—");

export const categoryLabel = (c) =>
  ({
    YOGA: "瑜伽",
    AEROBIC: "有氧",
    STRENGTH: "力量",
    PRIVATE: "私教"
  }[c] || c || "—");

export const msgTypeLabel = (t) =>
  ({
    EXPIRE: "到期提醒",
    BOOKING_OK: "预约成功",
    BOOKING_CANCEL: "预约取消"
  }[t] || t || "—");
