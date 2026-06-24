<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="申请人" prop="userId">
        <el-select v-model="queryParams.userId" placeholder="请选择申请人" clearable filterable
          style="width: 200px; background-color: #f5f7fa; color: #909399;">
          <el-option v-for="user in userOptions" :key="user.userId" :label="user.nickName" :value="user.userId" />
        </el-select>
      </el-form-item>
      <el-form-item label="实验室" prop="labId">
        <el-select v-model="queryParams.labId" placeholder="请选择实验室" clearable filterable>
          <el-option v-for="lab in labOptions" :key="lab.id" :label="lab.labName" :value="lab.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
          v-hasPermi="['reservation:reservation:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reservationList" @selection-change="handleSelectionChange"
      :row-class-name="rowClassName">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="申请人" align="center" prop="nickName" />
      <el-table-column label="实验室" align="center" prop="labName" width="130" />
      <el-table-column label="预约日期" align="center" prop="reservationDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.reservationDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="预约时间段" align="center" prop="timeSlot">
        <template #default="scope">
          <span v-if="scope.row.timeSlot === 1">8:00 - 10:00</span>
          <span v-else-if="scope.row.timeSlot === 2">10:00 - 12:00</span>
          <span v-else-if="scope.row.timeSlot === 3">14:30 - 16:30</span>
          <span v-else-if="scope.row.timeSlot === 4">16:30 - 18:30</span>
        </template>
      </el-table-column>
      <el-table-column label="预约用途" align="center" prop="purpose" />
      <el-table-column label="预约状态" align="center" prop="status">
        <template #default="scope">
          <span v-if="scope.row.status === 0">待审核</span>
          <span v-else-if="scope.row.status === 1">已通过</span>
          <span v-else-if="scope.row.status === 2">已拒绝</span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="createdAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="success" icon="Check" @click="handleApprove(scope.row)">同意</el-button>
          <el-button link type="danger" icon="Close" @click="handleReject(scope.row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="Reservation">
import { listReservation } from "@/api/reservation/reservation";
import { listUser } from "@/api/system/user";
import { listLabs_browse } from "@/api/labs_browse/labs_browse";
import { ElMessageBox } from 'element-plus';
import { reviewReservation } from "@/api/reservation/reservation";

const userOptions = ref([]);
const labOptions = ref([]);
const { proxy } = getCurrentInstance();

const reservationList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: null,
    labId: null,
    status: 0,
    teacherId: null,
  },
  rules: {}
});

const { queryParams, form, rules } = toRefs(data);

function rowClassName({ row }) {
  return row.status !== 0 ? 'row-hidden' : '';
}

/** 获取预约列表 */
function getList() {
  loading.value = true;
  listReservation(queryParams.value).then(response => {
    reservationList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 搜索 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 多选处理 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 导出 */
function handleExport() {
  proxy.download('reservation/reservation/export', {
    ...queryParams.value
  }, `reservation_${new Date().getTime()}.xlsx`);
}

/** 初始化下拉数据 */
function initSelectOptions() {
  listUser().then(res => {
    userOptions.value = (res.rows || []).map(user => ({
      userId: user.userId,
      nickName: user.nickName || `用户${user.userId}`
    }));
  });
  listLabs_browse().then(res => {
    labOptions.value = res.rows;
  });
}

/** 同意审核 */
function handleApprove(row) {
  ElMessageBox.prompt('请输入同意理由（可为空）', '审核通过', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValidator: () => true
  }).then(({ value }) => {
    reviewReservation({
      id: row.id,
      status: 1,
      labId: row.labId,
      reservationDate: row.reservationDate,
      timeSlot: row.timeSlot,
      reviewNote: value || ''
    }).then(() => {
      proxy.$modal.msgSuccess("审核通过");
      getList();
    });
  }).catch(() => { });
}

/** 拒绝审核 */
function handleReject(row) {
  ElMessageBox.prompt('请输入拒绝理由（不为空）', '审核拒绝', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '拒绝理由不能为空'
  }).then(({ value }) => {
    reviewReservation({
      id: row.id,
      status: 2,
      reviewNote: value
    }).then(() => {
      proxy.$modal.msgSuccess("审核已拒绝");
      getList();
    });
  }).catch(() => { });
}

onMounted(() => {
  initSelectOptions();
  getList();
});
</script>

<style scoped>
.row-hidden {
  display: none;
}
</style>