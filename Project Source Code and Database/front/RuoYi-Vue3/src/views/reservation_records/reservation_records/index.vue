<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="实验室" prop="labId">
        <el-select v-model="queryParams.labId" placeholder="请选择实验室" clearable filterable>
          <el-option v-for="lab in labOptions" :key="lab.id" :label="lab.labName" :value="lab.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="预约日期" prop="reservationDate">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="-"
          start-placeholder="开始日期" end-placeholder="结束日期" />
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

    <el-table v-loading="loading" :data="reservationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="申请人" align="center" prop="nickName" width="70" />
      <el-table-column label="实验室" align="center" prop="labName" width="170" />
      <el-table-column label="预约日期" align="center" prop="reservationDate" width="100">
        <template #default="scope">
          <span>{{ parseTime(scope.row.reservationDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="预约时间段" align="center" prop="timeSlot" width="110">
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
      <el-table-column label="审核教师" align="center" prop="teacherNickName" />
      <el-table-column label="审核时间" align="center" prop="reviewTime" width="100">
        <template #default="scope">
          <span>{{ parseTime(scope.row.reviewTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核意见" align="center" prop="reviewNote" />
      <el-table-column label="提交时间" align="center" prop="createdAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 数据可视化弹窗 -->
    <el-dialog v-model="showChartsDialog" title="预约数据可视化" width="80%">
      <div style="display: flex; flex-wrap: wrap; gap: 20px;">
        <div style="flex: 1; min-width: 400px; height: 400px;" ref="barChartRef"></div>
        <div style="flex: 1; min-width: 400px; height: 400px;" ref="funnelChartRef"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Reservation">
import { listReservation } from "@/api/reservation/reservation";
import { listUser } from "@/api/system/user";
import { listLabs_browse } from "@/api/labs_browse/labs_browse";
import useUserStore from '@/store/modules/user'
import { watch } from 'vue'

const userStore = useUserStore()
const userOptions = ref([]);
const labOptions = ref([]);
const currentUserId = ref(null)
const { proxy } = getCurrentInstance();

const reservationList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
// 新增日期范围响应式变量
const dateRange = ref([])
// 计算属性判断是否管理员
const isAdmin = computed(() => currentUserId.value === 1);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: null,
    labId: null,
    status: null,
    teacherId: null,
    beginReservationDate: null, // 新增
    endReservationDate: null    // 新增
  },
  rules: {
  }
});

const { queryParams, form, rules } = toRefs(data);

// 监听用户ID变化
watch(
  () => userStore.id,
  (newVal) => {
    currentUserId.value = newVal
    // 仅非管理员强制设置查询用户ID
    if (newVal && !isAdmin.value) {
      queryParams.value.userId = newVal
    }
    if (newVal) getList()
  },
  { immediate: true }
)

/** 查询实验室使用记录列表 */
/** 修改后的查询方法 */
function getList() {
  loading.value = true
  const params = {
    ...queryParams.value,
    // 管理员不传 userId，非管理员强制传当前 userId
    ...(!isAdmin.value && { userId: currentUserId.value })
  }

  listReservation(params).then(response => {
    // 管理员过滤掉 status == 0（待审核），非管理员不过滤
    reservationList.value = isAdmin.value
      ? response.rows.filter(item => item.status !== 0)
      : response.rows

    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    userId: null,
    labId: null,
    reservationDate: null,
    timeSlot: null,
    purpose: null,
    status: null,
    teacherId: null,
    reviewTime: null,
    reviewNote: null,
    createdAt: null
  };
  proxy.resetForm("reservationRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  // 处理日期参数
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.value.beginReservationDate = dateRange.value[0]
    queryParams.value.endReservationDate = dateRange.value[1]
  } else {
    queryParams.value.beginReservationDate = null
    queryParams.value.endReservationDate = null
  }

  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  dateRange.value = [] // 清空日期选择

  // 重置为初始查询参数
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    // 管理员不传userId，普通用户强制传当前userId
    ...(!isAdmin.value && { userId: currentUserId.value }),
    // 其他参数置空
    labId: null,
    status: null,
    teacherId: null,
    beginReservationDate: null,
    endReservationDate: null
  }

  getList()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}


/** 导出按钮操作 */
function handleExport() {
  proxy.download('reservation/reservation/export', {
    ...queryParams.value
  }, `reservation_${new Date().getTime()}.xlsx`)
}

// 获取用户和实验室下拉数据
function initSelectOptions() {
  listUser().then(res => {
    // 管理员显示全部用户，其他用户只显示自己
    const filteredUsers = isAdmin.value
      ? res.rows
      : res.rows.filter(u => u.userId === currentUserId.value)

    // 确保nickName字段存在
    userOptions.value = filteredUsers.map(user => ({
      userId: user.userId,
      nickName: user.nickName || `用户${user.userId}` // 默认值保障
    }))
  })

  listLabs_browse().then(res => {
    labOptions.value = res.rows
  })
}

watch(
  () => userStore.userId,
  (newVal) => {
    if (newVal) {
      // 自动关联当前用户昵称
      const currentUser = userOptions.value.find(u => u.userId === newVal);
      if (currentUser) {
        queryParams.value.userId = newVal;
        console.log('当前用户已更新:', currentUser.nickName);
      }
    }
  },
  { immediate: true, deep: true }
);

onMounted(() => {
  initSelectOptions();
});

getList();
</script>
