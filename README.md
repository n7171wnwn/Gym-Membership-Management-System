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
  - 微信小程序端骨架（登录、首页、课程预约示例）
  - JMeter 100 并发压测脚本模板

## 启动步骤

### 1) 启动 MySQL（推荐 Docker）

```bash
docker compose up -d
```

如果你本地已有 MySQL，执行：

```sql
source database/init.sql;
```

并确保账号密码与 `backend/src/main/resources/application.yml` 一致。
你也可以用环境变量覆盖：
`MYSQL_HOST` `MYSQL_PORT` `MYSQL_DB` `MYSQL_USERNAME` `MYSQL_PASSWORD`。

### 2) 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：`http://localhost:8080/api`

默认登录账号：

- 管理员：`admin / admin123`
- 会员：`member / member123`

可选环境开关：

- `app.redis-enabled=true` 启用 Redis 缓存
- `app.rabbitmq-enabled=true` 启用 RabbitMQ 同步

会员导出接口（管理员）：

- `GET /api/gym/members/export`

### 3) 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 目录结构

- `backend`：Spring Boot REST API
- `frontend`：Vue 前端页面
- `database/init.sql`：数据库初始化脚本
- `docker-compose.yml`：一键启动 MySQL
- `miniapp`：微信小程序端骨架
- `tests/jmeter/booking-100-users.jmx`：并发压测脚本
