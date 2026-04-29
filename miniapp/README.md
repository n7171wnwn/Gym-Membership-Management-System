# 微信小程序端（骨架）

本目录提供一个**最小可跑的微信小程序骨架**，用于对接后端已实现的会员端能力：

- 公告：`GET /api/gym/announcements`（无需登录）
- 手机号绑定登录：`POST /api/gym/auth/member/bind-phone`
- 微信 code 登录（可选）：`POST /api/gym/auth/wx-login`（需要配置 `app.wx-miniapp.app-id/app-secret`）

## 使用方式

1. 确保后端已启动：`http://localhost:8080/api`
2. 使用微信开发者工具导入本目录 `miniapp`
3. 预览运行后：
   - 首页会拉取公告列表
   - “绑定手机号登录”会调用后端接口拿到 JWT（示例 UI）

> 提示：当前是“对接骨架”，页面与交互可按毕业设计实际需求继续完善（课程列表、预约、我的卡、我的消息等）。

