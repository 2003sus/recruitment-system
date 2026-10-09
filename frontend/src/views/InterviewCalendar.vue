<template>
  <div class="page">
    <div class="topbar">
      <div class="topbar-left">
        <span class="logo">📅</span>
        <h1>面试排期</h1>
      </div>
      <div class="topbar-right">
        <el-button type="primary" @click="openAddDialog">+ 新增排期</el-button>
        <el-button plain @click="router.push('/')">← 招聘登记</el-button>
        <el-button plain @click="router.push('/stats')">📊 统计看板</el-button>
        <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <div class="content">
      <div class="card">
        <el-calendar v-model="calendarDate">
          <template #date-cell="{ data }">
            <div class="cal-cell" @click="handleDayClick(data)">
              <div class="cal-day">{{ data.day.split('-')[2] }}</div>
              <div v-if="dayMap[data.day]?.length" class="cal-badge">
                {{ dayMap[data.day].length }}
              </div>
            </div>
          </template>
        </el-calendar>
      </div>
    </div>

    <!-- 当天面试列表 -->
    <el-dialog v-model="dayDialogVisible" :title="`${selectedDay} 面试安排`" width="760px">
      <el-table :data="dayList" border stripe>
        <el-table-column prop="interviewTime" label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.interviewTime) }}</template>
        </el-table-column>
        <el-table-column prop="candidateName" label="候选人" width="100" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="jobName" label="岗位" width="110" />
        <el-table-column prop="interviewerName" label="面试官" width="110" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="dayList.length === 0" description="当天没有面试" />
      <template #footer>
        <el-button @click="dayDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="openAddDialog">+ 新增排期</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑排期 -->
    <el-dialog
      v-model="formVisible"
      :title="form.id ? '编辑排期' : '新增排期'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="候选人" required v-if="!form.id">
          <el-select
            v-model="form.candidateId"
            placeholder="选择候选人"
            filterable
            style="width: 100%;"
            @change="handleCandidateChange"
          >
            <el-option
              v-for="c in candidates"
              :key="c.id"
              :label="`${c.candidateName}（${c.mobile}）`"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="候选人">
          <el-input v-model="form.candidateName" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.mobile" disabled />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="form.jobName" disabled />
        </el-form-item>
        <el-form-item label="面试时间" required>
          <el-date-picker
            v-model="form.interviewTime"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="面试官">
          <el-input v-model="form.interviewerName" placeholder="面试负责人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%;">
            <el-option label="待面试" value="待面试" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已拒绝" value="已拒绝" />
            <el-option label="已取消" value="已取消" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const calendarDate = ref(new Date())
const schedules = ref<any[]>([])
const candidates = ref<any[]>([])

const dayDialogVisible = ref(false)
const selectedDay = ref('')

const formVisible = ref(false)
const form = ref<any>({
  id: null,
  candidateId: null,
  candidateName: '',
  mobile: '',
  jobName: '',
  interviewTime: '',
  interviewerName: '',
  status: '待面试',
  remark: ''
})

/** 每天有多少场面试 */
const dayMap = computed(() => {
  const m: Record<string, any[]> = {}
  for (const s of schedules.value) {
    const d = (s.interviewTime || '').slice(0, 10)
    if (!m[d]) m[d] = []
    m[d].push(s)
  }
  return m
})

const dayList = computed(() => dayMap.value[selectedDay.value] || [])

const fetchSchedules = async () => {
  const y = calendarDate.value.getFullYear()
  const m = calendarDate.value.getMonth()
  const start = `${y}-${String(m + 1).padStart(2, '0')}-01`
  const endDate = new Date(y, m + 1, 0)
  const end = `${y}-${String(m + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`
  const res = await request.get('/interview/list', { params: { start, end } })
  schedules.value = res.data
}

const fetchCandidates = async () => {
  const res = await request.get('/recruitment/list', {
    params: { page: 1, size: 1000 }
  })
  candidates.value = res.data.records
}

const handleDayClick = (data: any) => {
  selectedDay.value = data.day
  dayDialogVisible.value = true
}

const formatTime = (t: string) => (t || '').replace('T', ' ').slice(0, 16)

const statusType = (s: string) => {
  if (s === '已通过') return 'success'
  if (s === '已拒绝') return 'danger'
  if (s === '已取消') return 'info'
  return 'warning'
}

const openAddDialog = () => {
  form.value = {
    id: null,
    candidateId: null,
    candidateName: '',
    mobile: '',
    jobName: '',
    interviewTime: '',
    interviewerName: '',
    status: '待面试',
    remark: ''
  }
  formVisible.value = true
}

const openEditDialog = (row: any) => {
  form.value = { ...row }
  dayDialogVisible.value = false
  formVisible.value = true
}

const handleCandidateChange = (id: number) => {
  const c = candidates.value.find((x: any) => x.id === id)
  if (c) {
    form.value.candidateName = c.candidateName
    form.value.mobile = c.mobile
    form.value.jobName = c.jobName
  }
}

const handleSubmit = async () => {
  if (!form.value.candidateName) return ElMessage.warning('请选择候选人')
  if (!form.value.interviewTime) return ElMessage.warning('请选择面试时间')

  try {
    if (form.value.id) {
      await request.put(`/interview/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/interview', form.value)
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    await fetchSchedules()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除 ${row.candidateName} 的面试排期？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/interview/${row.id}`)
    ElMessage.success('删除成功')
    await fetchSchedules()
  })
}

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(async () => {
  await fetchSchedules()
  await fetchCandidates()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f2f5; }
.topbar {
  height: 64px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 32px; position: sticky; top: 0; z-index: 100;
}
.topbar-left { display: flex; align-items: center; gap: 12px; }
.logo { font-size: 24px; }
.topbar-left h1 { font-size: 18px; font-weight: 600; color: #1f2d3d; margin: 0; }
.topbar-right { display: flex; align-items: center; gap: 12px; }
.content { max-width: 1400px; margin: 0 auto; padding: 24px 32px; }
.card {
  background: #fff; border-radius: 12px; padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.cal-cell { position: relative; height: 100%; width: 100%; }
.cal-day { font-size: 14px; }
.cal-badge {
  position: absolute; top: 2px; right: 2px;
  background: #409eff; color: #fff; font-size: 11px;
  border-radius: 8px; padding: 1px 6px;
}
:deep(.el-calendar-day) { height: 70px; padding: 4px; }
</style>