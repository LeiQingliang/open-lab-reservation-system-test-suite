-- ============================================================
-- T29 数据库性能测试 SQL
-- ------------------------------------------------------------
-- 用途: 慢查询配置、核心查询执行计划分析、索引建议、大数据量
--       生成、连接池与缓冲池命中率监控。
-- 适用: MySQL 8.0。EXPLAIN ANALYZE 需 MySQL 8.0.18+。
-- 用法:
--   mysql -u root -proot open_lab_reservation < db_performance.sql
--   或在客户端中按节选择执行。
-- 注意:
--   1. 第 5 节会生成大量数据, 默认仅创建存储过程, CALL 已注释,
--      执行前请务必备份数据库。
--   2. 数据库名 / 表名以实际 SUT 为准, 必要时调整。
-- ============================================================

-- 1. 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 0.5;  -- 超过0.5秒记录
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- 2. 查看当前慢查询配置
SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';

-- ============================================================
-- 3. 核心查询性能分析
-- ============================================================

-- 3.1 预约列表多表联查 (最复杂查询)
EXPLAIN ANALYZE
SELECT
    r.id, r.user_id, r.lab_id, r.reservation_date,
    r.time_slot, r.purpose, r.status, r.teacher_id,
    r.review_time, r.review_note, r.created_at,
    u.nick_name AS nickName,
    l.lab_name AS labName,
    t.nick_name AS teacherNickName
FROM reservation r
LEFT JOIN sys_user u ON r.user_id = u.user_id
LEFT JOIN labs l ON r.lab_id = l.id
LEFT JOIN sys_user t ON r.teacher_id = t.user_id
WHERE r.status = 0
ORDER BY r.created_at DESC
LIMIT 10;

-- 3.2 冲突检测查询 (核心业务查询)
EXPLAIN ANALYZE
SELECT * FROM reservation
WHERE lab_id = 1
  AND reservation_date = '2026-07-01'
  AND time_slot = 1
  AND status = 0;

-- 3.3 排期列表查询
EXPLAIN ANALYZE
SELECT id, lab_id, date, time_slot, is_available
FROM lab_schedule
WHERE date = CURDATE();

-- 3.4 实验室模糊查询
EXPLAIN ANALYZE
SELECT id, lab_name, location, capacity, status, description, created_at, updated_at
FROM labs
WHERE lab_name LIKE CONCAT('%', '软件', '%');

-- ============================================================
-- 4. 索引建议
-- ============================================================

-- 检查当前索引
SHOW INDEX FROM reservation;
SHOW INDEX FROM lab_schedule;
SHOW INDEX FROM labs;

-- 建议添加的索引 (基于查询分析)
-- reservation表: 冲突检测需复合索引
ALTER TABLE reservation
  ADD INDEX idx_lab_date_slot_status (lab_id, reservation_date, time_slot, status);

-- reservation表: 用户查询
ALTER TABLE reservation
  ADD INDEX idx_user_status (user_id, status);

-- reservation表: 日期范围查询
ALTER TABLE reservation
  ADD INDEX idx_date (reservation_date);

-- lab_schedule表: 日期查询
ALTER TABLE lab_schedule
  ADD INDEX idx_date_lab (date, lab_id);

-- ============================================================
-- 5. 数据量测试
-- ============================================================

-- 插入10万条预约记录进行大数据量测试
DROP PROCEDURE IF EXISTS generate_test_reservations;
DELIMITER $$
CREATE PROCEDURE generate_test_reservations(IN num_rows INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_lab INT;
    DECLARE random_user INT;
    DECLARE random_slot INT;
    DECLARE random_date DATE;
    DECLARE random_status INT;

    WHILE i < num_rows DO
        SET random_lab = FLOOR(1 + RAND() * 5);
        SET random_user = FLOOR(1 + RAND() * 10);
        SET random_slot = FLOOR(1 + RAND() * 4);
        SET random_date = DATE_ADD('2026-01-01', INTERVAL FLOOR(RAND() * 365) DAY);
        SET random_status = FLOOR(RAND() * 3);

        INSERT INTO reservation (user_id, lab_id, reservation_date, time_slot, purpose, status, created_at)
        VALUES (random_user, random_lab, random_date, random_slot,
                CONCAT('性能测试数据_', i), random_status, NOW());

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 生成10万条测试数据 (执行前请备份!)
-- CALL generate_test_reservations(100000);

-- 清理测试数据
-- DELETE FROM reservation WHERE purpose LIKE '性能测试数据_%';

-- ============================================================
-- 6. 连接池监控
-- ============================================================

-- 查看当前连接数
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Threads_running';
SHOW STATUS LIKE 'Max_used_connections';

-- 查看连接详情
SHOW PROCESSLIST;

-- 查看InnoDB缓冲池命中率
SHOW STATUS LIKE 'Innodb_buffer_pool_read%';

-- 计算命中率
SELECT
  (1 - (SELECT VARIABLE_VALUE FROM performance_schema.global_status
        WHERE VARIABLE_NAME='Innodb_buffer_pool_reads')
      / (SELECT VARIABLE_VALUE FROM performance_schema.global_status
         WHERE VARIABLE_NAME='Innodb_buffer_pool_read_requests')) * 100
  AS buffer_pool_hit_rate;

-- ============================================================
-- 附录: 测试数据清理
-- ============================================================

-- 清理性能测试产生的预约数据
-- DELETE FROM reservation WHERE purpose LIKE '%性能测试%' OR purpose LIKE '%AB测试%' OR purpose LIKE '%并发%';
-- DELETE FROM lab_schedule WHERE note LIKE '%性能测试%';
-- DELETE FROM labs WHERE lab_name LIKE '%TestLab_TC%';
