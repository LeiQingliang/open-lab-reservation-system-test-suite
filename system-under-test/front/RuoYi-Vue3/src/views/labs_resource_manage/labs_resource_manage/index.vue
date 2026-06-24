<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" label-width="100px" v-show="showSearch" class="demo-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="实验室名称" prop="labName">
            <el-input v-model="queryParams.labName" placeholder="请输入实验室名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="实验室地址" prop="location">
            <el-input v-model="queryParams.location" placeholder="请输入实验室地址" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="可容纳人数" prop="capacity">
            <el-input v-model="queryParams.capacity" placeholder="请输入实验室可容纳人数" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="12" class="form-btns">
          <el-form-item>
            <el-button icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            <el-button type="primary" icon="Pointer" @click="handlePublishTimeSlots">发布实验室可预约时段</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['labs_browse:labs_browse:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
          v-hasPermi="['labs_browse:labs_browse:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['labs_browse:labs_browse:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
          v-hasPermi="['labs_browse:labs_browse:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="labs_resource_manageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="实验室名称" align="center" prop="labName" width="170" />
      <el-table-column label="实验室地址" align="center" prop="location" width="90" />
      <el-table-column label="可容纳人数" align="center" prop="capacity" width="90" />
      <el-table-column label="实验室简介" align="center" prop="description" width="400" />
      <el-table-column label="实验室信息最后更新时间" align="center" prop="updatedAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updatedAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['labs_browse:labs_browse:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
            v-hasPermi="['labs_browse:labs_browse:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改实验室资源管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="labs_resource_manageRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="实验室名称" prop="labName">
          <el-input v-model="form.labName" placeholder="请输入实验室名称" />
        </el-form-item>
        <el-form-item label="实验室地址" prop="location">
          <el-input v-model="form.location" placeholder="请输入实验室地址" />
        </el-form-item>
        <el-form-item label="实验室可容纳人数" prop="capacity">
          <el-input v-model="form.capacity" placeholder="请输入实验室可容纳人数" />
        </el-form-item>
        <el-form-item label="实验室简介或备注" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 发布预约 -->
    <el-dialog title="发布可预约时段" v-model="timeSlotsDialogVisible" width="500px" append-to-body>
      <el-form ref="timeSlotsFormRef" :model="timeSlotsForm" :rules="timeSlotsRules" label-width="100px">
        <el-form-item label="选择日期" prop="date">
          <el-date-picker v-model="timeSlotsForm.date" type="date" placeholder="选择日期" :disabled-date="disabledDate"
            value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="选择时段" prop="timeSlots">
          <el-checkbox-group v-model="timeSlotsForm.timeSlots">
            <el-checkbox :label="1">8:00-10:00</el-checkbox>
            <el-checkbox :label="2">10:00-12:00</el-checkbox>
            <el-checkbox :label="3">14:30-16:30</el-checkbox>
            <el-checkbox :label="4">16:30-18:30</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="timeSlotsDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitTimeSlots">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Labs_resource_manage">
import { listLabs_resource_manage, getLabs_resource_manage, delLabs_resource_manage, addLabs_resource_manage, updateLabs_resource_manage } from "@/api/labs_resource_manage/labs_resource_manage";
import dayjs from 'dayjs';
import { publishLabTimeSlots } from '@/api/labs_schedule/labs_schedule'

const { proxy } = getCurrentInstance();

const labs_resource_manageList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

// 新增的发布时段相关状态（完全独立）
const timeSlotsDialogVisible = ref(false);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    labName: null,
    location: null,
    capacity: null,
  },
  rules: {
    labName: [
      { required: true, message: "实验室名称，如“软件工程实验室”不能为空", trigger: "blur" }
    ],
    location: [
      { required: true, message: "实验室地址", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询实验室资源管理列表 */
function getList() {
  loading.value = true;
  listLabs_resource_manage(queryParams.value).then(response => {
    labs_resource_manageList.value = response.rows;
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
    labName: null,
    location: null,
    capacity: null,
    description: null,
    createdAt: null,
    updatedAt: null
  };
  proxy.resetForm("labs_resource_manageRef");
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

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加实验室资源管理";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getLabs_resource_manage(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改实验室资源管理";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["labs_resource_manageRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateLabs_resource_manage(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addLabs_resource_manage(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除实验室资源管理编号为"' + _ids + '"的数据项？').then(function () {
    return delLabs_resource_manage(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => { });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('labs_resource_manage/labs_resource_manage/export', {
    ...queryParams.value
  }, `labs_resource_manage_${new Date().getTime()}.xlsx`)
}

const timeSlotsForm = reactive({
  date: '',
  timeSlots: []
});

const timeSlotsRules = reactive({
  date: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  timeSlots: [
    { required: true, message: '请至少选择一个时段', trigger: 'change' }
  ]
});

/* 新增的发布时段方法（完全独立） */
const handlePublishTimeSlots = () => {
  if (ids.value.length === 0) {
    proxy.$modal.msgWarning("请至少选择一条实验室数据");
    return;
  }

  timeSlotsForm.date = '';
  timeSlotsForm.timeSlots = [];
  timeSlotsDialogVisible.value = true;
};

const disabledDate = (time) => {
  // 修改为：只能选择明天及以后的日期
  return dayjs(time).isBefore(dayjs().startOf('day'), 'day');
};

const submitTimeSlots = () => {
  if (!timeSlotsForm.date) {
    proxy.$modal.msgWarning("请选择日期");
    return;
  }

  if (timeSlotsForm.timeSlots.length === 0) {
    proxy.$modal.msgWarning("请至少选择一个时段");
    return;
  }

  // 准备请求数据
  const postData = {
    labIds: ids.value,       // 所有选中的实验室ID数组
    date: timeSlotsForm.date, // 选择的日期
    timeSlots: timeSlotsForm.timeSlots // 选择的时段数组 [1,2,3...]
  };

  // 调用API
  publishLabTimeSlots(postData)
    .then(response => {
      proxy.$modal.msgSuccess("发布成功");
      timeSlotsDialogVisible.value = false;
    })
    .catch(error => {
      proxy.$modal.msgError("发布失败: " + (error.message || "未知错误"));
    });
};

getList();

</script>

<style scoped>
.form-btns {
  display: flex;
  align-items: center;
  height: 100%;
}
</style>
