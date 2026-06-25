# 探索式测试 Charter — M3 预约管理

> 基于会话的探索式测试(SBET)。带着 mission 在时间盒内自由探索。
> 实际结果填入 [`../execution-log-template.md`](../execution-log-template.md)。
>
> M3 是历史缺陷最密集的模块(5/12 缺陷:BUG-001/002/003/005/006),建议优先且充分探索。

## Mission(任务)

探索预约提交与查询全流程,重点攻击**冲突检测、时间段/日期/用途/labId 的输入校验**等核心业务规则,
确认这些是预约系统的核心价值点是否成立。

## 时间盒

90 分钟(建议,本模块加长)。

## 覆盖区域

- 前端:预约提交弹窗(`/apply`)、我的预约记录(`/reservation_records`)。
- 接口:`POST /reservation/reservation/insert`、`GET /reservation/reservation/list`、
  `GET /reservation/reservation/{id}`、管理员增改删。
- 数据:`reservation` 表(status:0 待审核 / 1 通过 / 2 拒绝)。

## 测试点子(取自真实用例)

- 正常提交:status 强制为 0、createdAt 自动设置;timeSlot=1 最早 / =4 最晚;长文本/特殊字符用途
  (FT-RSV-001~005、BIZ-009/010)。
- 前端传 status=1 试图绕过审核 → 系统应强制覆盖为 0(VAL-RSV-024/025、FT-RSV-017)。
- 查询:分页、按日期范围、按 status、按 labId 筛选;详情含 nickName/labName 联表
  (FT-RSV-006~011、DB-02/03)。
- 输入校验:timeSlot=5/0/-1/null/99999;date 过去/null/当天/明天;
  labId null/0/99999/-1;purpose ""/null/纯空格/超长(VAL-RSV-001~022)。

## 已知缺陷观察点(本模块为重灾区)

> 状态约定:下列"已修复"依据**当前被测源码**(`ReservationController.insertReservation` 顶部注释);
> 历史曾为未修复的项见 [缺陷清单](../../../docs/05-defects/T29_缺陷清单.md)。已修复项做**回归确认**(应被拒绝)。

- **BUG-001 无冲突检测**(源码已加冲突检测,但⚠️**结果存疑**):同一 lab+date+slot 重复提交,第二次预期应被拒绝。
  分别用同用户、不同用户验证(FT-11 / BIZ-01 / BIZ-02 历史 FAIL)。
  ⚠️ 注意:insert 路径的冲突查询未设 id,而 `selectConflictingReservations` SQL 末尾含 `AND id != #{id}`,
  MySQL 中 `id != NULL` 恒为 UNKNOWN → 冲突检测**可能在 insert 路径不命中**,第二次仍可能成功。
  **请以本地实际复现结果为准**,这正是探索式测试的价值点。
- **BUG-002 timeSlot 无范围校验**(已修复):提交 timeSlot=99/0,预期应被拒绝(VAL-06/07 历史 FAIL)。
- **BUG-003 过去日期未拦截**(已修复):提交 date=2020-01-01,预期应被拒绝;若仍成功则为回退(VAL-08 / BIZ-04 历史 FAIL)。
- **BUG-005 labId 校验**(已修复为非空校验):labId=null 应被拒绝;
  但 labId=99999(非空但不存在)是否被拦截取决于实现,以本地复现为准(BIZ-06 历史 500)。
- **BUG-006 空用途可提交**(已修复):purpose="",预期应被拒绝(VAL-05 历史 FAIL)。
- **空必填项返回 500 而非友好提示**:userId/labId/date/timeSlot 为 null 时历史返回 500
  (VAL-01~04 历史 FAIL),观察错误处理是否改善。

## 启发式提示

- 冲突检测是核心:务必构造"同 lab + 同 date + 同 slot"的第二次提交,而非仅改一个维度。
- 区分"已修复"与"未修复"缺陷:M3 缺陷当前源码均已修复,做**回归确认**(预期应被拒绝);
  唯 BUG-001 因上述 SQL 陷阱**结果存疑**,务必实测。
- SQL 注入(purpose=`'DROP TABLE`)/XSS(`<script>`)历史 PASS,可作为安全旁路顺带验证。
- 直连 MySQL `localhost:3307` 核对 `reservation` 表实际写入值(尤其 time_slot、status)。

## 会话产出(填入执行日志)

- 覆盖到的功能区:`__________`
- 主要观察 / 发现:`__________`
- 复现的已知缺陷:`__________`
- 新疑点:`__________`
