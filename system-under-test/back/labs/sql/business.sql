-- ----------------------------------------------------------------------------
-- 开放实验室预约系统 · 业务表与初始化数据
-- ----------------------------------------------------------------------------
-- 说明：RuoYi 自带脚本 ry_20250417.sql 仅含系统表，业务模块
--       （实验室浏览 labs / 排课 lab_schedule / 预约 reservation）三张表
--       的 DDL 在原仓库缺失，导致业务页面运行期报 "Table doesn't exist"。
--       本脚本据 labs-management 模块 Mapper.xml 与 domain 实体补齐，
--       并插入演示数据与业务菜单，使系统开箱即用。
--       列名/字段已与 LabsMapper / LabScheduleMapper / ReservationMapper 对齐。
-- 字符集：utf8mb4   引擎：InnoDB
-- ----------------------------------------------------------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1、实验室资源表 labs
-- ----------------------------
DROP TABLE IF EXISTS `labs`;
CREATE TABLE `labs` (
  `id`          BIGINT(20)    NOT NULL AUTO_INCREMENT              COMMENT '实验室ID',
  `lab_name`    VARCHAR(255)  DEFAULT NULL                        COMMENT '实验室名称，如"软件工程实验室"',
  `location`    VARCHAR(255)  DEFAULT NULL                        COMMENT '实验室地址，如"教学楼A305"',
  `capacity`    BIGINT(20)    DEFAULT NULL                        COMMENT '可容纳人数',
  `status`      INT(4)        DEFAULT 1                           COMMENT '状态（1正常 0停用）',
  `description` VARCHAR(1000) DEFAULT NULL                        COMMENT '实验室简介或备注',
  `created_at`  DATETIME      DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
  `updated_at`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室资源';

-- ----------------------------
-- 2、实验室排课表 lab_schedule
-- ----------------------------
DROP TABLE IF EXISTS `lab_schedule`;
CREATE TABLE `lab_schedule` (
  `id`           BIGINT(20)   NOT NULL AUTO_INCREMENT             COMMENT '排课记录ID',
  `lab_id`       BIGINT(20)   DEFAULT NULL                       COMMENT '实验室ID（labs.id）',
  `date`         DATE         DEFAULT NULL                       COMMENT '排课日期',
  `time_slot`    BIGINT(20)   DEFAULT NULL                       COMMENT '时间段编号（1:8-10,2:10-12,3:14:30-16:30,4:16:30-18:30）',
  `is_available` INT(4)       DEFAULT 1                          COMMENT '是否可预约（1可用 0不可用）',
  `note`         VARCHAR(255) DEFAULT NULL                       COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lab_date_slot` (`lab_id`, `date`, `time_slot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室排课';

-- ----------------------------
-- 3、实验室预约表 reservation
-- ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `id`               BIGINT(20)   NOT NULL AUTO_INCREMENT         COMMENT '预约记录ID',
  `user_id`          BIGINT(20)   DEFAULT NULL                   COMMENT '申请人ID（sys_user.user_id）',
  `lab_id`           BIGINT(20)   DEFAULT NULL                   COMMENT '实验室ID（labs.id）',
  `reservation_date` DATE         DEFAULT NULL                   COMMENT '预约日期',
  `time_slot`        BIGINT(20)   DEFAULT NULL                   COMMENT '时间段编号',
  `purpose`          VARCHAR(500) DEFAULT NULL                   COMMENT '预约用途',
  `status`           BIGINT(20)   DEFAULT 0                      COMMENT '状态（0待审核 1已通过 2已拒绝）',
  `teacher_id`       BIGINT(20)   DEFAULT NULL                   COMMENT '审核教师ID（sys_user.user_id）',
  `review_time`      DATETIME     DEFAULT NULL                   COMMENT '审核时间',
  `review_note`      VARCHAR(500) DEFAULT NULL                   COMMENT '审核意见',
  `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP      COMMENT '提交时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_date_slot` (`lab_id`, `reservation_date`, `time_slot`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室预约记录';

-- ----------------------------
-- 演示数据：实验室
-- ----------------------------
INSERT INTO `labs` (`lab_name`, `location`, `capacity`, `status`, `description`) VALUES
('软件工程实验室', '教学楼A305', 60, 1, '配备60台计算机，适合软件开发课程'),
('网络技术实验室', '教学楼B201', 40, 1, '配备网络设备，适合网络课程'),
('嵌入式实验室',   '实验楼C101', 30, 1, '配备嵌入式开发板'),
('大数据实验室',   '教学楼D402', 50, 1, '配备高性能计算集群'),
('人工智能实验室', '实验楼E301', 35, 1, '配备GPU服务器');

-- ----------------------------
-- 演示数据：排课（今天/明天，时间段1-4）
-- ----------------------------
INSERT INTO `lab_schedule` (`lab_id`, `date`, `time_slot`, `is_available`, `note`) VALUES
(1, CURDATE(),                      1, 1, '上午第一节'),
(1, CURDATE(),                      2, 1, '上午第二节'),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 1, NULL),
(2, CURDATE(),                      1, 1, NULL),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 3, 1, NULL),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 2, 1, NULL);

-- ----------------------------
-- 演示数据：预约（admin 提交，待审核/已通过各一条）
-- ----------------------------
INSERT INTO `reservation` (`user_id`, `lab_id`, `reservation_date`, `time_slot`, `purpose`, `status`) VALUES
(1, 1, CURDATE(),                      1, '软件工程课程设计上机', 0),
(1, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 3, '网络实验',           1);

-- ----------------------------------------------------------------------------
-- 业务菜单（sys_menu）：补充原仓库缺失的业务模块入口
-- 注：超级管理员 admin（user_id=1）默认拥有全部菜单，无需 sys_role_menu。
--     menu_id 使用 3000+ 区间，避开 RuoYi 自带菜单 ID。
-- ----------------------------------------------------------------------------
DELETE FROM `sys_menu` WHERE `menu_id` BETWEEN 3000 AND 3099;

-- 顶级目录
INSERT INTO `sys_menu` (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3000, '实验室预约', 0, 4, 'lab', NULL, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '实验室预约管理目录');

-- 实验室浏览
INSERT INTO `sys_menu` (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3010, '实验室浏览', 3000, 1, 'labs_browse', 'labs_browse/labs_browse/index', '', '', 1, 0, 'C', '0', '0', 'labs_browse:labs_browse:list', 'build', 'admin', sysdate(), '实验室浏览菜单'),
(3011, '实验室查询', 3010, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'labs_browse:labs_browse:query',  '#', 'admin', sysdate(), ''),
(3012, '实验室新增', 3010, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'labs_browse:labs_browse:add',    '#', 'admin', sysdate(), ''),
(3013, '实验室修改', 3010, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'labs_browse:labs_browse:edit',   '#', 'admin', sysdate(), ''),
(3014, '实验室删除', 3010, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'labs_browse:labs_browse:remove', '#', 'admin', sysdate(), ''),
(3015, '实验室导出', 3010, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'labs_browse:labs_browse:export', '#', 'admin', sysdate(), '');

-- 排课管理
INSERT INTO `sys_menu` (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3020, '排课管理', 3000, 2, 'apply', 'apply/apply/index', '', '', 1, 0, 'C', '0', '0', 'apply:apply:list', 'date', 'admin', sysdate(), '实验室排课菜单'),
(3021, '排课查询', 3020, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'apply:apply:query',  '#', 'admin', sysdate(), ''),
(3022, '排课新增', 3020, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'apply:apply:add',    '#', 'admin', sysdate(), ''),
(3023, '排课修改', 3020, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'apply:apply:edit',   '#', 'admin', sysdate(), ''),
(3024, '排课删除', 3020, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'apply:apply:remove', '#', 'admin', sysdate(), ''),
(3025, '排课导出', 3020, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'apply:apply:export', '#', 'admin', sysdate(), '');

-- 预约管理
INSERT INTO `sys_menu` (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3030, '预约管理', 3000, 3, 'reservation', 'reservation/reservation/index', '', '', 1, 0, 'C', '0', '0', 'reservation:reservation:list', 'form', 'admin', sysdate(), '实验室预约菜单'),
(3031, '预约查询', 3030, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'reservation:reservation:query',  '#', 'admin', sysdate(), ''),
(3032, '预约新增', 3030, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'reservation:reservation:add',    '#', 'admin', sysdate(), ''),
(3033, '预约修改', 3030, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'reservation:reservation:edit',   '#', 'admin', sysdate(), ''),
(3034, '预约删除', 3030, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'reservation:reservation:remove', '#', 'admin', sysdate(), ''),
(3035, '预约导出', 3030, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'reservation:reservation:export', '#', 'admin', sysdate(), '');

-- 预约审核
INSERT INTO `sys_menu` (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3040, '预约审核', 3000, 4, 'reservation_review', 'reservation_review/reservation_review/index', '', '', 1, 0, 'C', '0', '0', 'reservation:reservation:list', 'checkbox', 'admin', sysdate(), '预约审核菜单');

SET FOREIGN_KEY_CHECKS = 1;
