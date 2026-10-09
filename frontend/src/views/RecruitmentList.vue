<template>
  <div class="page">
    <div class="topbar">
      <div class="topbar-left">
        <span class="logo">📋</span>
        <h1>人事招聘登记</h1>
      </div>
      <div class="topbar-right">
        <span class="user">HR</span>
        <el-button plain size="small" @click="router.push('/stats')">
          📊 统计看板
        </el-button>
        <el-button plain size="small" @click="router.push('/interview')">
          📅 面试排期
        </el-button>
        <el-button type="danger" plain size="small" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </div>

    <div class="content">
      <!-- 搜索区 -->
      <SearchBar
        :search="search"
        :depts="depts"
        :selected-count="selectedIds.length"
        @search="fetchList"
        @reset="resetSearch"
        @add="openAddDialog"
        @batch-delete="handleBatchDelete"
        @batch-edit="openBatchEditDialog"
        @export="handleExport"
        @import="handleImport"
        @import-help="importHelpVisible = true"
      />

      <!-- 表格 -->
      <CandidateTable
        ref="candidateTableRef"
        :list="list"
        :jobs="jobs"
        :depts="depts"
        :total="total"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        @quick-update="handleQuickUpdate"
        @refresh="fetchList"
        @edit="openEditDialog"
        @delete="handleDelete"
        @confirm-hire="handleConfirmHire"
        @row-upload="handleRowUpload"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <EditDialog
      v-model="dialogVisible"
      v-model:form="form"
      :title="dialogTitle"
      :jobs="jobs"
      :depts="depts"
      @submit="handleSubmit"
    />

    <!-- 批量修改弹窗 -->
    <BatchEditDialog
      v-model="batchDialogVisible"
      :depts="depts"
      @confirm="handleBatchUpdate"
    />

    <!-- 导入说明弹窗 -->
    <ImportHelpDialog v-model="importHelpVisible" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import SearchBar from '../components/SearchBar.vue'
import CandidateTable from '../components/CandidateTable.vue'
import EditDialog from '../components/EditDialog.vue'
import BatchEditDialog from '../components/BatchEditDialog.vue'
import ImportHelpDialog from '../components/ImportHelpDialog.vue'

const router = useRouter()
const candidateTableRef = ref()
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const search = ref({ keyword: '', passed: '', hired: '', deptId: null })
const depts = ref<any[]>([])
const jobs = ref<string[]>([])
const selectedIds = ref<number[]>([])
const sortField = ref('id')
const sortOrder = ref('asc')

const dialogVisible = ref(false)
const dialogTitle = ref('新增')
const form = ref({
  id: null,
  jobName: '',
  candidateName: '',
  mobile: '',
  interviewerName: '',
  passed: '待确认',
  hired: '待确认',
  deptId: null,
  deptName: '',
  resumePath: ''
})

const batchDialogVisible = ref(false)
const importHelpVisible = ref(false)

const fetchList = async () => {
  const res = await request.get('/recruitment/list', {
    params: {
      keyword: search.value.keyword,
      passed: search.value.passed,
      hired: search.value.hired,
      deptId: search.value.deptId,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      page: currentPage.value,
      size: pageSize.value
    }
  })
  list.value = res.data.records
  total.value = res.data.total
}

const fetchDepts = async () => {
  const res = await request.get('/recruitment/depts')
  depts.value = res.data
}

const fetchJobs = async () => {
  const res = await request.get('/recruitment/jobs')
  jobs.value = res.data
}

const resetSearch = () => {
  search.value = { keyword: '', passed: '', hired: '', deptId: null }
  currentPage.value = 1
  selectedIds.value = []
  candidateTableRef.value?.clearSelection()
  fetchList()
}

const handleSelectionChange = (rows: any[]) => {
  selectedIds.value = rows.map((r: any) => r.id)
}

const handleSortChange = ({ prop, order }: any) => {
  sortField.value = prop || 'id'
  sortOrder.value = order === 'descending' ? 'desc' : 'asc'
  fetchList()
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定删除选中的 ${selectedIds.value.length} 条记录？`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.delete('/recruitment/batch/delete', {
        data: selectedIds.value
      })
      ElMessage.success('删除成功')
      selectedIds.value = []
      candidateTableRef.value?.clearSelection()
      fetchList()
    } catch (e: any) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  })
}

const openBatchEditDialog = () => {
  batchDialogVisible.value = true
}

const handleBatchUpdate = async (form: any) => {
  try {
    await request.put('/recruitment/batch/field', {
      ids: selectedIds.value,
      passed: form.passed || null,
      hired: form.hired || null,
      deptId: form.deptId || null
    })
    ElMessage.success('修改成功')
    batchDialogVisible.value = false
    selectedIds.value = []
    candidateTableRef.value?.clearSelection()
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

const handleExport = async () => {
  try {
    const token = localStorage.getItem('token')
    const res = await fetch('http://localhost:8080/api/recruitment/export', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(selectedIds.value)
    })

    const blob = await res.blob()
    const objectUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = '招聘记录.xlsx'
    link.click()
    URL.revokeObjectURL(objectUrl)
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

const handleImport = async (options: any) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const res = await request.post('/recruitment/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success(`导入成功 ${res.data.success} 条，失败 ${res.data.fail} 条`)
    fetchList()
  } catch (e: any) {
    ElMessage.error('导入失败')
  }
}

const handleQuickUpdate = async (row: any) => {
  try {
    const dept = depts.value.find((d: any) => d.id === row.deptId)
    if (dept) {
      row.deptName = dept.name
    }

    await request.put(`/recruitment/${row.id}/field`, {
      passed: row.passed,
      hired: row.hired,
      deptId: row.deptId,
      jobName: row.jobName
    })
    ElMessage.success('已保存')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '保存失败')
    fetchList()
  }
}

const openAddDialog = () => {
  dialogTitle.value = '新增招聘记录'
  form.value = {
    id: null,
    jobName: '',
    candidateName: '',
    mobile: '',
    interviewerName: '',
    passed: '待确认',
    hired: '待确认',
    deptId: null,
    deptName: '',
    resumePath: ''
  }
  dialogVisible.value = true
}

const openEditDialog = (row: any) => {
  dialogTitle.value = '编辑招聘记录'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    const selectedDept = depts.value.find((d: any) => d.id === form.value.deptId)
    if (selectedDept) {
      form.value.deptName = selectedDept.name
    }

    if (form.value.id) {
      await request.put(`/recruitment/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/recruitment', form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除 ${row.candidateName}？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/recruitment/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  })
}

const handleConfirmHire = (row: any) => {
  ElMessageBox.confirm(
    `确认给 ${row.candidateName}（${row.mobile}）创建 CRM 账号？`,
    '确认入职',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.post(`/recruitment/${row.id}/confirm-hire`)
      ElMessage.success('创建成功')
      fetchList()
    } catch (e: any) {
      ElMessage.error(e.response?.data?.message || '创建失败')
      fetchList()
    }
  })
}

const handleRowUpload = async ({ options, row }: { options: any; row: any }) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const res = await request.post('/recruitment/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    await request.put(`/recruitment/${row.id}`, {
      ...row,
      resumePath: res.data.path
    })

    ElMessage.success('上传成功')
    fetchList()
  } catch (e: any) {
    ElMessage.error('上传失败')
    options.onError(e)
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(() => {
  fetchList()
  fetchDepts()
  fetchJobs()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f0f2f5;
}

.topbar {
  height: 64px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  font-size: 24px;
}

.topbar-left h1 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
  margin: 0;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user {
  font-size: 14px;
  color: #606266;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.content {
  max-width: 1700px;
  margin: 0 auto;
  padding: 24px 32px;
}
</style>