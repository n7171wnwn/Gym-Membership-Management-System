const CAT = { YOGA: '瑜伽', AEROBIC: '有氧', STRENGTH: '力量', PRIVATE: '私教' };

function categoryLabel(c) {
  return CAT[c] || c || '课程';
}

function bookingStatusLabel(s) {
  if (s === 'PENDING') return '待审核';
  if (s === 'APPROVED') return '待上课';
  if (s === 'COMPLETED') return '已完成';
  if (s === 'CANCELLED') return '已取消';
  return s || '—';
}

function formatTime(iso) {
  if (!iso) return '';
  return String(iso).replace('T', ' ').slice(0, 16);
}

module.exports = { categoryLabel, bookingStatusLabel, formatTime };
