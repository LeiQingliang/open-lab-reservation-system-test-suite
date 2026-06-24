<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="申请人" prop="userId">
        <el-select v-model="queryParams.userId" placeholder="请选择申请人" clearable filterable>
          <el-option v-for="user in userOptions" :key="user.userId" :label="user.nickName" :value="user.userId" />
        </el-select>
      </el-form-item>
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
      <el-col :span="1.5">
        <el-button type="success" plain icon="PieChart" @click="showChartsDialog = true">数据可视化</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reservationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="申请人" align="center" prop="nickName" />
      <el-table-column label="实验室" align="center" prop="labName" width="150" />
      <el-table-column label="预约日期" align="center" prop="reservationDate" width="150">
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
      <el-table-column label="审核教师" align="center" prop="teacherNickName" />
      <el-table-column label="审核时间" align="center" prop="reviewTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.reviewTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
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
import { ref, reactive, toRefs, getCurrentInstance, onMounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';
import { listReservation } from "@/api/reservation/reservation";
import { listUser } from "@/api/system/user";
import { listLabs_browse } from "@/api/labs_browse/labs_browse";
import { parseTime } from "@/utils/ruoyi";

const userOptions = ref([]);
const labOptions = ref([]);
const { proxy } = getCurrentInstance();

const fullList = ref([]);
const reservationList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const showChartsDialog = ref(false);
const barChartRef = ref(null);
const funnelChartRef = ref(null);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 50,
    userId: null,
    labId: null,
    status: null,
    teacherId: null,
    beginReservationDate: null,
    endReservationDate: null
  },
  rules: {}
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listReservation(queryParams.value).then(response => {
    fullList.value = response.rows; // 保存所有记录
    reservationList.value = response.rows.filter(item => item.status === 1); // 只展示 status=1 的记录
    total.value = reservationList.value.length;
    loading.value = false; // ✅ 一定要关闭 loading
  }).catch(() => {
    loading.value = false; // 加个 catch 兜底，防止出错时一直 loading
  });
}

function handleQuery() {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.value.beginReservationDate = dateRange.value[0]
    queryParams.value.endReservationDate = dateRange.value[1]
  } else {
    queryParams.value.beginReservationDate = null
    queryParams.value.endReservationDate = null
  }
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef");
  dateRange.value = [];
  queryParams.value.beginReservationDate = null;
  queryParams.value.endReservationDate = null;
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleExport() {
  proxy.download('reservation/reservation/export', {
    ...queryParams.value
  }, `reservation_${new Date().getTime()}.xlsx`);
}

function initSelectOptions() {
  listUser().then(res => {
    userOptions.value = res.rows || [];
  });
  listLabs_browse().then(res => {
    labOptions.value = res.rows;
  });
}

function renderCharts() {
  nextTick(() => {
    const barChart = echarts.init(barChartRef.value);
    const funnelChart = echarts.init(funnelChartRef.value);

    const all = fullList.value.length;
    const passed = fullList.value.filter(item => item.status === 1).length;

    const labUsage = {};
    const totalCount = reservationList.value.length;
    let passedCount = 0;

    reservationList.value.forEach(item => {
      if (item.labName) {
        labUsage[item.labName] = (labUsage[item.labName] || 0) + 1;
      }
      if (item.status === 1) {
        passedCount += 1;
      }
    });

    const barOption = {
      title: { text: '实验室使用次数' },
      tooltip: {},
      xAxis: { type: 'category', data: Object.keys(labUsage) },
      yAxis: { type: 'value' },
      series: [{
        name: '使用次数',
        type: 'bar',
        data: Object.values(labUsage)
      }]
    };

    const funnelOption = {
      title: { text: '预约转化率' },
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [
        {
          name: '转化率',
          type: 'funnel',
          left: '10%',
          top: 60,
          bottom: 60,
          width: '80%',
          minSize: '0%',
          maxSize: '100%',
          sort: 'descending',
          label: { show: true, position: 'inside' },
          data: [
            { value: all, name: '总预约数' },
            { value: passed, name: '已通过' }
          ]
        }
      ]
    };

    barChart.setOption(barOption);
    funnelChart.setOption(funnelOption);
  });
}

watch(showChartsDialog, val => {
  if (val) renderCharts();
});

onMounted(() => {
  initSelectOptions();
  getList();
});
</script>
