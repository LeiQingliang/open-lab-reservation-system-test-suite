# 探索式测试 Charter — M1 实验室管理

> 基于会话的探索式测试(SBET)。带着 mission 在时间盒内自由探索,记录观察,不必逐字照步骤。
> 实际结果填入 [`../execution-log-template.md`](../execution-log-template.md) 的探索式会话小节。

## Mission(任务)

探索实验室的增删改查与导出,验证基础 CRUD 是否健壮,并重点考察**输入校验缺口**与
**删除时的关联数据处理**。

## 时间盒

60 分钟(建议)。

## 覆盖区域

- 前端:实验室浏览页 `/labs_browse`(列表、详情、新增/编辑/删除、导出)。
- 接口:`GET /labs_browse/labs_browse/list`(@Anonymous)、`GET /labs_browse/labs_browse/{id}`、
  对应的 POST/PUT/DELETE。
- 数据:`labs` 表;与 `reservation` / `lab_schedule` 的关联。

## 测试点子(取自真实用例)

- 新增实验室:完整字段 / 仅必填字段 / 全字段含 status+description(FT-LAB-001~003)。
- 列表查询:无参数全量、按 labName 模糊、按 location 搜索(FT-LAB-004~006)。
- 详情:ID 存在 / ID=99999 不存在(FT-LAB-007~008)。
- 编辑:正常改名/改地点、改 status、改不存在的 ID(FT-LAB-009~011)。
- 删除:单个 / 批量 / 空数组(幂等)(FT-LAB-012~014)。
- 导出 Excel,核对字段完整(FT-LAB-015)。
- 匿名访问列表(未登录直接调 list 应返回数据,@Anonymous)(FT-LAB-016 / API-04)。
- 输入校验:labName 为 null / 空 / 重复;capacity 为 null / -1 / 0(VAL-LAB-001~006)。

## 已知缺陷观察点

- **空名称未校验**:新增实验室 labName 留空仍创建成功(报告 FT-02 / VAL-09 历史 FAIL)。确认是否仍存在。
- **BUG-008 删除无关联检查**(未修复):删除有预约/排期关联的实验室后,
  `reservation` / `lab_schedule` 中的关联数据未级联处理。删除后查历史预约,观察孤儿数据。
- capacity 负数/0 是否被接受(边界,VAL-LAB-005/006)。

## 启发式提示

- 用"等价类 + 边界值"快速覆盖 labName 长度、capacity 数值域。
- 删除后立刻查询关联数据(可直连 MySQL `localhost:3307` 看 `reservation.lab_id`)。
- 注意匿名 list 与受保护写接口的权限差异。

## 会话产出(填入执行日志)

- 覆盖到的功能区:`__________`
- 主要观察 / 发现:`__________`
- 复现的已知缺陷:`__________`
- 新疑点:`__________`
