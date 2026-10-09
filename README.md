# 人事招聘登记系统

一个类 Excel 的招聘登记管理系统。HR 登记面试信息，人工确认入职后，系统自动使用姓名、手机号和所选部门在 CRM 中创建账号，并防止重复创建。

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + ECharts |
| 后端 | Java 17 + Spring Boot 4 + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 文件转换 | LibreOffice |
| 其他 | pdfjs-dist（PDF 预览）、FastExcel（Excel 导入导出） |

## 功能列表

### 核心功能

- ✅ 招聘登记表（类 Excel，分页展示）
- ✅ 新增 / 编辑 / 删除 / 批量删除
- ✅ 按姓名 / 手机号 / 岗位搜索
- ✅ 是否通过 / 是否入职（下拉直改）
- ✅ 入职部门下拉（保存 deptId + 名称）
- ✅ 简历上传（PDF / DOC / DOCX / JPG / PNG / GIF）
- ✅ Word → PDF 自动转换（LibreOffice）
- ✅ 简历预览 / 下载 / 删除
- ✅ 确认入职 → 自动创建 CRM 账号
- ✅ CRM 状态机（未创建 / 创建中 / 已创建 / 失败）
- ✅ 防止重复创建（crmUserId 存在则按钮禁用）
- ✅ 失败原因记录 + 允许重试
- ✅ HR 登录 + 接口鉴权

### 可选功能（已实现）

- ✅ Excel 导入 / 导出（支持选中导出）
- ✅ 批量修改
- ✅ 统计看板（ECharts 柱状图 + 饼图）
- ✅ 面试排期 + 日历视图
- ✅ 统一圆角主题 + 中文界面
- ✅ 表格内直改（改完自动保存）

## 项目结构

```
recruitment-system/
├── backend/recruitment-backend/          # 后端 Spring Boot 项目
│   ├── src/main/java/com/example/recruitmentbackend/
│   │   ├── config/                       # 配置类
│   │   ├── controller/                   # HTTP 接口
│   │   ├── service/                      # 业务逻辑
│   │   ├── mapper/                       # 数据访问
│   │   ├── entity/                       # 实体类
│   │   └── util/                         # 工具类
│   ├── src/main/resources/
│   │   └── application.properties        # 配置文件
│   └── build.gradle
├── frontend/                             # 前端 Vue 项目
│   ├── src/
│   │   ├── views/                        # 页面
│   │   ├── components/                   # 组件
│   │   ├── api/request.ts                # axios 封装
│   │   ├── router/index.ts               # 路由
│   │   ├── assets/                       # 全局样式
│   │   └── main.ts
│   └── package.json
├── init.sql                              # 数据库初始化脚本
└── README.md
```

## 快速开始

### 前置要求

- JDK 17+
- Node.js 22+
- MySQL 8.0+
- LibreOffice（Word 转 PDF 用）

### 1. 初始化数据库

```bash
mysql -u root -p < init.sql
```

### 2. 启动后端

```bash
cd backend/recruitment-backend
./gradlew bootRun
```

后端跑在 `http://localhost:8080`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端跑在 `http://localhost:5173`。

### 4. 登录

- 用户名：`hr`
- 密码：`123456`

## 配置文件说明

`backend/recruitment-backend/src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/recruitment?...
spring.datasource.username=root
spring.datasource.password=你的密码

upload.dir=${user.dir}/uploads/recruitment

spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## 核心接口

| 方法 | URL | 作用 |
|------|-----|------|
| POST | `/api/auth/login` | 登录 |
| GET | `/api/recruitment/list` | 招聘列表（分页） |
| POST | `/api/recruitment` | 新增 |
| PUT | `/api/recruitment/{id}` | 编辑 |
| DELETE | `/api/recruitment/{id}` | 删除 |
| PUT | `/api/recruitment/{id}/field` | 表格内直改 |
| POST | `/api/recruitment/{id}/confirm-hire` | 确认入职 + 创建 CRM |
| POST | `/api/recruitment/upload` | 上传简历 |
| POST | `/api/recruitment/export` | 导出 Excel |
| POST | `/api/recruitment/import` | 导入 Excel |
| GET | `/api/recruitment/stats/overview` | 统计概览 |
| GET | `/api/recruitment/stats/by-job` | 各岗位统计 |
| GET | `/api/recruitment/stats/by-dept` | 各部门统计 |
| GET | `/api/interview/list` | 面试排期查询 |
| POST | `/api/interview` | 新增排期 |
| PUT | `/api/interview/{id}` | 编辑排期 |
| DELETE | `/api/interview/{id}` | 删除排期 |

## 关键设计

### 1. CRM 状态机

```
未创建 → 创建中 → 已创建
              ↘ 失败 → 可重试
```

用 `crm_sync_status` 独立字段记录，和 `hired` 字段分离。

### 2. 防止重复创建

- 前端：`crmUserId != null` 时按钮禁用
- 后端：调用 CRM 前检查 `crmUserId`，存在则报错
- 调用期间状态设为 `创建中`，防止并发

### 3. 文件存储

- 上传路径：`${user.dir}/uploads/recruitment/`
- `user.dir` 是 Java 系统属性，指向项目启动目录
- Word 文件上传后自动转 PDF

## 常见问题

### Q: 上传失败，报路径不存在？

A: 检查 `application.properties` 里的 `upload.dir`。用 `${user.dir}/uploads/recruitment`，不要用 `./uploads/recruitment`（Tomcat 会解析到临时目录）。

### Q: Word 转 PDF 乱码？

A: 确认装了 LibreOffice，且 `soffice` 命令在 PATH 里。

### Q: 前端 401 未登录？

A: token 过期，重新登录。

### Q: 后端改代码不生效？

A: Java 必须重启后端，热更新不生效。

## 交付物

- ✅ 后端源码（Spring Boot）
- ✅ 前端源码（Vue 3）
- ✅ 数据库初始化脚本（`init.sql`）
- ✅ 项目说明（`README.md`）

## License

仅供学习与演示使用。