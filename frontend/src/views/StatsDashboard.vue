<template>
  <div class="page">
    <div class="topbar">
      <div class="topbar-left">
        <span class="logo">📊</span>
        <h1>统计看板</h1>
      </div>
      <div class="topbar-right">
        <el-button plain @click="router.push('/')">← 返回招聘登记</el-button>
        <el-button plain @click="router.push('/interview')">📅 面试排期</el-button>
        <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <div class="content">
      <!-- 顶部 4 张卡片 -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-label">总候选人数</div>
          <div class="stat-value">{{ overview.total || 0 }}</div>
        </div>
        <div class="stat-card success">
          <div class="stat-label">已通过面试</div>
          <div class="stat-value">{{ overview.passed || 0 }}</div>
          <div class="stat-sub">通过率 {{ overview.passRate || 0 }}%</div>
        </div>
        <div class="stat-card primary">
          <div class="stat-label">已确认入职</div>
          <div class="stat-value">{{ overview.hired || 0 }}</div>
          <div class="stat-sub">入职率 {{ overview.hireRate || 0 }}%</div>
        </div>
        <div class="stat-card warning">
          <div class="stat-label">待确认</div>
          <div class="stat-value">{{ overview.pending || 0 }}</div>
        </div>
      </div>

      <!-- 柱状图：各岗位 -->
      <div class="card">
        <h3>各岗位人数 / 通过 / 入职</h3>
        <div ref="jobChartRef" class="chart"></div>
      </div>

      <!-- 两个图并排 -->
      <div class="chart-row">
        <div class="card">
          <h3>各部门人数</h3>
          <div ref="deptChartRef" class="chart"></div>
        </div>
        <div class="card">
          <h3>整体通过率</h3>
          <div ref="pieChartRef" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '../api/request'

const router = useRouter()

const overview = ref<any>({})
const jobChartRef = ref<HTMLElement>()
const deptChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let jobChart: echarts.ECharts | null = null
let deptChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const fetchOverview = async () => {
  const res = await request.get('/recruitment/stats/overview')
  overview.value = res.data
}

const renderJobChart = async () => {
  const res = await request.get('/recruitment/stats/by-job')
  const data = res.data as any[]
  jobChart = echarts.init(jobChartRef.value!)
  jobChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['总人数', '已通过', '已入职'] },
    grid: { left: 40, right: 20, top: 40, bottom: 60 },
    xAxis: { type: 'category', data: data.map(d => d.jobName) },
    yAxis: { type: 'value' },
    series: [
      { name: '总人数', type: 'bar', data: data.map(d => d.total), itemStyle: { color: '#409eff' } },
      { name: '已通过', type: 'bar', data: data.map(d => d.passed), itemStyle: { color: '#67c23a' } },
      { name: '已入职', type: 'bar', data: data.map(d => d.hired), itemStyle: { color: '#e6a23c' } }
    ]
  })
}

const renderDeptChart = async () => {
  const res = await request.get('/recruitment/stats/by-dept')
  const data = res.data as any[]
  deptChart = echarts.init(deptChartRef.value!)
  deptChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 60 },
    xAxis: { type: 'category', data: data.map(d => d.deptName) },
    yAxis: { type: 'value' },
    series: [
      { name: '总人数', type: 'bar', data: data.map(d => d.total), itemStyle: { color: '#409eff' } },
      { name: '已入职', type: 'bar', data: data.map(d => d.hired), itemStyle: { color: '#67c23a' } }
    ]
  })
}

const renderPieChart = () => {
  const total = overview.value.total || 0
  const passed = overview.value.passed || 0
  const failed = total - passed
  pieChart = echarts.init(pieChartRef.value!)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: passed, name: '已通过', itemStyle: { color: '#67c23a' } },
        { value: failed, name: '未通过/待确认', itemStyle: { color: '#f56c6c' } }
      ],
      label: { formatter: '{b}: {c} ({d}%)' }
    }]
  })
}

const handleResize = () => {
  jobChart?.resize()
  deptChart?.resize()
  pieChart?.resize()
}

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(async () => {
  await fetchOverview()
  await nextTick()
  await renderJobChart()
  await renderDeptChart()
  renderPieChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  jobChart?.dispose()
  deptChart?.dispose()
  pieChart?.dispose()
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
.stat-cards {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 20px;
}
.stat-card {
  background: #fff; border-radius: 12px; padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04); border-left: 4px solid #909399;
}
.stat-card.success { border-left-color: #67c23a; }
.stat-card.primary { border-left-color: #409eff; }
.stat-card.warning { border-left-color: #e6a23c; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 600; color: #1f2d3d; }
.stat-sub { font-size: 12px; color: #909399; margin-top: 4px; }
.card {
  background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.card h3 { font-size: 15px; font-weight: 600; color: #1f2d3d; margin: 0 0 16px 0; }
.chart { width: 100%; height: 320px; }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
</style>