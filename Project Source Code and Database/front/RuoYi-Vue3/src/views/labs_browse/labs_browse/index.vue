<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
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
            <el-input v-model="queryParams.capacity" placeholder="请输入可容纳人数" clearable @keyup.enter="handleQuery" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24" style="text-align: right;">
          <el-form-item class="search-btn-group">
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-table v-loading="loading" :data="labs_browseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="实验室ID" align="center" prop="id" width="80"/>
      <el-table-column label="实验室名称" align="center" prop="labName" width="170"/>
      <el-table-column label="实验室地址" align="center" prop="location" width="90"/>
      <el-table-column label="容纳人数" align="center" prop="capacity" width="80"/>
      <el-table-column label="实验室简介或备注" align="center" prop="description" />
      <el-table-column label="实验室信息最后更新时间" align="center" prop="updatedAt" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updatedAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改实验室资源浏览1对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="labs_browseRef" :model="form" :rules="rules" label-width="80px">
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
  </div>
</template>

<script setup name="Labs_browse">
import { listLabs_browse, getLabs_browse, delLabs_browse, addLabs_browse, updateLabs_browse } from "@/api/labs_browse/labs_browse";

const { proxy } = getCurrentInstance();

const labs_browseList = ref([]);
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
  },
  rules: {
    labName: [
      { required: true, message: "实验室名称", trigger: "blur" }
    ],
    location: [
      { required: true, message: "实验室地址", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询实验室资源浏览1列表 */
function getList() {
  loading.value = true;
  listLabs_browse(queryParams.value).then(response => {
    labs_browseList.value = response.rows;
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
  proxy.resetForm("labs_browseRef");
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


getList();
</script>

<style scoped>
/* 添加样式优化布局 */
.search-btn-group {
  margin-right: 10px;
}
.el-row {
  margin-bottom: 15px;
}
.el-form-item {
  margin-bottom: 0;
}
</style>