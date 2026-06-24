<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="实验室" prop="labId">
        <el-select v-model="queryParams.labId" placeholder="请选择实验室" clearable filterable>
          <el-option v-for="lab in labOptions" :key="lab.id" :label="lab.labName" :value="lab.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期" prop="date">
        <el-date-picker clearable v-model="queryParams.date" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="时间段" prop="timeSlot">
        <el-select v-model="queryParams.timeSlot" placeholder="请选择时间段" clearable>
          <el-option label="8:00 - 10:00" :value="1" />
          <el-option label="10:00 - 12:00" :value="2" />
          <el-option label="14:00 - 16:00" :value="3" />
          <el-option label="16:00 - 18:00" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="applyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="实验室" align="center">
        <template #default="scope">
          <span>
            {{ getLabName(scope.row.labId) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="日期" align="center" prop="date" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.date, '{y}-{m}-{d}') }}</span>
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
      <el-table-column label="实验室状态" align="center" prop="isAvailable">
        <template #default="scope">
          <el-tag :type="scope.row.isAvailable === 1 ? 'success' : 'danger'">
            {{ scope.row.isAvailable === 1 ? '可约' : '不可约' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="openReserveDialog(scope.row)">
            申请预约
          </el-button>
        </template>
      </el-table-column>

    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 预约用途 -->
    <el-dialog :title="title" v-model="reserveDialogVisible" align-center width="500px" append-to-body>
      <el-form :model="reserveForm" ref="reserveFormRef" label-width="80px">
        <el-form-item label="预约用途" prop="purpose" :rules="[{ required: true, message: '请输入用途', trigger: 'blur' }]">
          <el-input v-model="reserveForm.purpose" type="textarea" placeholder="请输入预约用途" />
        </el-form-item>
      </el-form>
      <el-form ref="applyRef" :model="form" :rules="rules" label-width="80px">
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reserveDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitReservation">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Apply">
import { listApply, getApply, delApply, addApply, updateApply } from "@/api/apply/apply";
import { listLabs_browse } from "@/api/labs_browse/labs_browse";
import { insertReservation } from "@/api/reservation/reservation";
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance();
const userStore = useUserStore()
const applyList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const labOptions = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    labId: null,
    date: null,
    timeSlot: null,
    isAvailable: null,
    note: null
  },
  rules: {
    labId: [
      { required: true, message: "实验室ID不能为空", trigger: "blur" }
    ],
    date: [
      { required: true, message: "日期不能为空", trigger: "blur" }
    ],
    timeSlot: [
      { required: true, message: "预约时间段编号不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

const reserveDialogVisible = ref(false);
const reserveForm = reactive({
  labId: null,
  userId: null,
  reservationDate: null,
  timeSlot: null,
  purpose: ''
});
const reserveFormRef = ref();

function openReserveDialog(row) {
  reserveForm.labId = row.labId;
  reserveForm.reservationDate = row.date;
  reserveForm.timeSlot = row.timeSlot;
  reserveForm.purpose = '';
  // 假设你登录后将 userId 存在 localStorage
  reserveForm.userId = proxy?.$store?.state?.user?.id || localStorage.getItem("userId");
  reserveDialogVisible.value = true;
}

function submitReservation() {
  reserveFormRef.value.validate(valid => {
    if (valid) {
      const payload = {
        labId: reserveForm.labId,
        userId: userStore.id,
        reservationDate: reserveForm.reservationDate,
        timeSlot: reserveForm.timeSlot,
        purpose: reserveForm.purpose,
        status: 0
      };
      insertReservation(payload).then(res => {
        proxy.$modal.msgSuccess("预约申请成功");
        reserveDialogVisible.value = false;
      });
    }
  });
}

function getLabName(labId) {
  const lab = labOptions.value.find(l => l.id === labId);
  return lab ? lab.labName : labId;
}

// 获取用户和实验室下拉数据
function initSelectOptions() {
  listLabs_browse().then(res => {
    labOptions.value = res.rows
  })
}

/** 查询预约实验室列表 */
function getList() {
  loading.value = true;
  listApply(queryParams.value).then(response => {
    applyList.value = response.rows.map(item => {
      const lab = labOptions.value.find(l => l.id === item.labId);
      return {
        ...item,
        labName: lab ? lab.labName : item.labId // 找不到就显示原来的 ID
      };
    });
    total.value = response.total;
    loading.value = false;
  });
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
    labId: null,
    date: null,
    timeSlot: null,
    isAvailable: null,
    note: null
  };
  proxy.resetForm("applyRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}


onMounted(async () => {
  await initSelectOptions(); // 等待实验室列表加载完成
  getList(); // 然后再加载 applyList
});

</script>