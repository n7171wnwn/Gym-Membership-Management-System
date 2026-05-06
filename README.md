# 健身房会员管理系统（Vue + SpringBoot + MySQL）

这是按你要求重构后的前后端分离版本。

## 技术栈

- 前端：Vue 3 + Vite + Axios
- 后端：Spring Boot 2.7 + Spring Data JPA
- 数据库：MySQL 8.0

## 已实现功能（对齐开题核心模块）

- 会员管理：新增会员、查看会员、等级展示、健康追踪
- 教练与课程管理：教练信息维护、课程与教练绑定、课程余量显示
- 课程预约：防重复预约、库存扣减、乐观锁版本字段
- 消费与等级：消费记录、自动等级计算（Bronze/Silver/Gold）
- 智能提醒：定时任务 + 手动触发到期提醒，提醒日志留存
- 双端同步示例：同步日志记录（源端/目标端/动作/状态）
- 统计分析：看板指标、课程销量排行接口
- 权限认证：JWT 登录鉴权（管理员/会员）
- 阶段二增强：
  - 角色细粒度权限控制（管理员/会员）
  - Redis 缓存（可开关，默认关闭时使用本地缓存）
  - RabbitMQ 双端同步消息（可开关，默认关闭）
  - EasyExcel 导出会员报表接口
  - 微信小程序端骨架（登录、首页公告示例）
  - JMeter 100 并发压测脚本模板

## 环境要求

- **JDK 8+**、**Maven 3.6+**（后端）
- **Node.js 18+**（推荐，前端；Node 16 可能仅有依赖警告）
- **MySQL 5.7/8.0** 或 **Docker Desktop**（数据库）

## 启动步骤（按顺序）

### 1) 准备数据库

**方式 A：Docker（与 `docker-compose.yml` 一致，root 密码 `123456`，库名 `gym_system`）**

```bash
docker compose up -d
```

> 若本机未安装 Docker 或命令不可用，请用方式 B。

**方式 B：本机已安装的 MySQL**

1. 创建库并导入初始化脚本（在 MySQL 客户端中执行 `database/init.sql`，或 `mysql -uroot -p < database/init.sql`）。
2. 账号密码需与 `backend/src/main/resources/application.yml` 中一致；默认配置为：
   - 用户：`root`
   - 密码：`123456`
   - 库名：`gym_system`
   - 端口：`3306`

**若本机 root 密码不是 `123456`（尤其 Windows 本机 MySQL）**，不要改代码里明文密码，用环境变量启动后端（见下节）。

可用环境变量覆盖连接：`MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DB`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`。

### 2) 启动后端

```bash
cd backend
mvn spring-boot:run
```

**Windows PowerShell（本机 MySQL 密码与配置不一致时）**：

```powershell
cd backend
$env:MYSQL_PASSWORD='你的MySQL密码'
mvn spring-boot:run
```

- 服务地址：`http://localhost:8080/api`（已带 `context-path: /api`）
- 启动成功日志中应出现 `Started GymMembershipApplication`。
- 浏览器自测（可选）：`http://localhost:8080/api/gym/announcements` 能返回数据即表示接口可访问。

**默认登录账号（Web 管理端 / 接口）**

- 管理员：`admin` / `admin123`
- 会员：`member` / `member123`

**可选能力（默认关闭，见 `application.yml`）**

- `app.redis-enabled=true`：启用 Redis
- `app.rabbitmq-enabled=true`：启用 RabbitMQ
- `app.wx-miniapp.app-id` / `app.wx-miniapp.app-secret`：微信小程序 `code2Session`（不配则小程序走手机号绑定登录）

**管理员导出会员（需登录管理员）**

- `GET /api/gym/members/export`

### 3) 启动前端（管理端页面）

```bash
cd frontend
npm install
npm run dev
```

- 地址：`http://localhost:5173`
- 通过 Vite 代理请求后端，需先保证后端已启动。

### 4) 微信小程序（可选）

1. 用**微信开发者工具**打开目录 `miniprogram`（或根目录下 `miniapp` 骨架，以你实际使用的目录为准）。
2. 在 `miniprogram/app.js`（或 `miniapp/app.js`）中，将 `globalData.apiBase` 设为：
   - **开发者工具模拟器**：可用 `http://127.0.0.1:8080/api`
   - **真机预览 / 真机调试**：填**运行后端的电脑在当前 Wi-Fi 下的 IPv4**（Windows：`ipconfig` 里 WLAN 对应地址），例如 `http://192.168.x.x:8080/api`，不要用已失效或其它设备的 IP。
3. 开发者工具：**详情 → 本地设置 → 勾选「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」**（本地 HTTP 调试必开）。
4. 确保手机与电脑**同一 Wi-Fi**，本机防火墙放行 **8080** 端口。

**小程序登录说明**：手机号绑定接口要求该手机号已在后台登记为会员；微信一键登录需配置小程序 `app-id` / `app-secret`。

## 目录结构

- `backend`：Spring Boot REST API
- `frontend`：Vue 管理端（Vite）
- `database/init.sql`：数据库初始化脚本
- `docker-compose.yml`：一键启动 MySQL（Docker）
- `miniapp`：微信小程序最小骨架（用户名密码 / 手机号绑定示例）
- `miniprogram`：微信小程序（示例页面更完整时可选用）
- `tests/jmeter/booking-100-users.jmx`：并发压测脚本
