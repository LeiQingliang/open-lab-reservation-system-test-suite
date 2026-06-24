<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" label-width="100px" v-show="showSearch">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="实验室名称" prop="labName">
            <el-input v-model="queryParams.labName" placeholder="请输入实验室名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="实验室地址" prop="location">
            <el-input v-model="queryParams.location" placeholder="请输入实验室地址" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="可容纳人数" prop="capacity">
            <el-input v-model="queryParams.capacity" placeholder="请输入实验室可容纳人数" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
          v-hasPermi="['labs_browse/labs_browse:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
          v-hasPermi="['labs_browse/labs_browse:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="labs_updateList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="实验室ID" align="center" prop="id" width="80"/>
      <el-table-column label="实验室名称" align="center" prop="labName" width="180"/>
      <el-table-column label="实验室地址" align="center" prop="location" width="90"/>
      <el-table-column label="可容纳人数" align="center" prop="capacity" width="90"/>
      <el-table-column label="实验室简介" align="center" prop="description" width="340"/>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['labs_browse:labs_browse:edit']">修改</el-button>
          <el-button link :type="scope.row.status === 1 ? 'danger' : 'success'"
            :icon="scope.row.status === 1 ? 'Close' : 'Check'" @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改维护实验室信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="labs_updateRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="实验室名称" prop="labName">
          <el-input v-model="form.labName" placeholder="请输入实验室名称" disabled />
        </el-form-item>
        <el-form-item label="实验室地址" prop="location">
          <el-input v-model="form.location" placeholder="请输入实验室地址" disabled />
        </el-form-item>
        <el-form-item label="实验室可容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="1000" placeholder="请输入人数" />
        </el-form-item>
        <el-form-item label="实验室简介" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入简介" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Labs_update">
import { addLabs_update, delLabs_update, getLabs_update, listLabs_update, updateLabs_update } from "@/api/labs_update/labs_update";

const { proxy } = getCurrentInstance();

const labs_updateList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    labName: null,
    location: null,
    capacity: null,
    status: null
  },
  rules: {
    labName: [
      { required: true, message: "实验室名称", trigger: "blur" }
    ],
    location: [
      { required: true, message: "实验室地址", trigger: "blur" }
    ],
    status: [
      { required: true, message: "状态不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询维护实验室信息列表 */
function getList() {
  loading.value = true;
  listLabs_update(queryParams.value).then(response => {
    labs_updateList.value = response.rows;
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
    updatedAt: null,
    status: null
  };
  proxy.resetForm("labs_updateRef");
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

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getLabs_update(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改实验室信息";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["labs_updateRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateLabs_update(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addLabs_update(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 修改状态 */
function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1;
  const statusText = newStatus === 1 ? "启用" : "禁用";
  proxy.$modal.confirm(`确定要${statusText}该实验室吗？`).then(() => {
    const updated = { ...row, status: newStatus };
    updateLabs_update(updated).then(() => {
      proxy.$modal.msgSuccess(`${statusText}成功`);
      getList(); // 重新刷新列表
    });
  }).catch(() => { });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('labs/labs_browse/export', {
    ...queryParams.value
  }, `labs_update_${new Date().getTime()}.xlsx`)
}

getList();
</script>
