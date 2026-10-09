-- ============================================================
-- 人事招聘登记系统 - 数据库初始化脚本
-- 说明：先执行本脚本，再启动后端
-- ============================================================

CREATE DATABASE IF NOT EXISTS recruitment
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE recruitment;

-- ============================================================
-- 1. HR 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS hr_user (
  id bigint NOT NULL AUTO_INCREMENT,
  username varchar(50) NOT NULL,
  password varchar(100) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 2. 招聘候选人表
-- ============================================================
CREATE TABLE IF NOT EXISTS recruitment_candidate (
  id bigint NOT NULL AUTO_INCREMENT,
  job_name varchar(100) NOT NULL COMMENT '岗位',
  candidate_name varchar(50) NOT NULL COMMENT '面试人姓名',
  mobile varchar(20) NOT NULL COMMENT '手机号（唯一）',
  resume_path varchar(500) DEFAULT NULL COMMENT '简历文件路径',
  interviewer_name varchar(50) NOT NULL COMMENT '面试负责人',
  passed varchar(20) DEFAULT '待确认' COMMENT '是否通过：待确认/是/否',
  hired varchar(20) DEFAULT '待确认' COMMENT '是否入职：待确认/是/否',
  dept_id bigint DEFAULT NULL COMMENT 'CRM 部门 ID',
  dept_name varchar(100) DEFAULT NULL COMMENT '部门名称',
  crm_sync_status varchar(20) DEFAULT '未创建' COMMENT 'CRM 状态：未创建/创建中/已创建/失败',
  crm_user_id bigint DEFAULT NULL COMMENT 'CRM 用户 ID',
  crm_error varchar(500) DEFAULT NULL COMMENT 'CRM 失败原因',
  crm_sync_time datetime DEFAULT NULL COMMENT 'CRM 创建时间',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY mobile (mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 3. 面试排期表
-- ============================================================
CREATE TABLE IF NOT EXISTS interview_schedule (
  id bigint NOT NULL AUTO_INCREMENT,
  candidate_id bigint DEFAULT NULL COMMENT '关联 recruitment_candidate.id',
  candidate_name varchar(50) NOT NULL,
  mobile varchar(20) NOT NULL,
  job_name varchar(100) DEFAULT NULL,
  interview_time datetime NOT NULL COMMENT '面试时间',
  interviewer_name varchar(50) DEFAULT NULL,
  status varchar(20) DEFAULT '待面试' COMMENT '待面试/已通过/已拒绝/已取消',
  remark varchar(500) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_interview_time (interview_time),
  KEY idx_candidate_id (candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 4. 默认 HR 账号
-- ============================================================
INSERT IGNORE INTO hr_user (username, password) VALUES ('hr', '123456');

-- ============================================================
-- 5. 测试数据（可选，正式部署可删除）
-- ============================================================
INSERT IGNORE INTO recruitment_candidate
  (job_name, candidate_name, mobile, interviewer_name, passed, hired, dept_id, dept_name, crm_sync_status)
VALUES
  ('一线销售', '张三', '13800000001', '李经理', '待确认', '待确认', NULL, NULL, '未创建'),
  ('一线销售', '李四', '13800000002', '李经理', '待确认', '待确认', NULL, NULL, '未创建'),
  ('一线销售', '王五', '13800000003', '李经理', '是', '待确认', NULL, NULL, '未创建'),
  ('二线销售', '赵六', '13800000004', '王经理', '是', '是', 102, '二线销售', '未创建'),
  ('二线销售', '钱七', '13800000005', '王经理', '否', '待确认', NULL, NULL, '未创建'),
  ('交付专员', '孙八', '13800000006', '赵经理', '待确认', '待确认', NULL, NULL, '未创建'),
  ('交付专员', '周九', '13800000007', '赵经理', '是', '是', 103, '交付专员', '未创建'),
  ('技术开发', '吴十', '13800000008', '陈经理', '是', '待确认', NULL, NULL, '未创建'),
  ('技术开发', '郑十一', '13800000009', '陈经理', '待确认', '待确认', NULL, NULL, '未创建'),
  ('产品经理', '王十二', '13800000010', '刘经理', '否', '待确认', NULL, NULL, '未创建'),
  ('产品经理', '冯十三', '13800000011', '刘经理', '是', '是', 105, '人事部', '未创建'),
  ('一线销售', '陈十四', '13800000012', '李经理', '待确认', '待确认', NULL, NULL, '未创建'),
  ('一线销售', '褚十五', '13800000013', '李经理', '是', '待确认', NULL, NULL, '未创建'),
  ('二线销售', '卫十六', '13800000014', '王经理', '待确认', '待确认', NULL, NULL, '未创建'),
  ('交付专员', '蒋十七', '13800000015', '赵经理', '是', '是', 103, '交付专员', '未创建');

-- ============================================================
-- 完成
-- ============================================================
SELECT '数据库初始化完成' AS message;
SELECT COUNT(*) AS candidate_count FROM recruitment_candidate;
SELECT COUNT(*) AS interview_count FROM interview_schedule;
SELECT COUNT(*) AS hr_user_count FROM hr_user;