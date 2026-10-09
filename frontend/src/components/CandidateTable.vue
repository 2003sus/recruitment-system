<template>
  <div class="card table-card">
    <el-table
      ref="tableRef"
      :data="list"
      border
      stripe
      style="width: 100%"
      row-key="id"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column type="selection" width="50" align="center" :reserve-selection="true" />
      <el-table-column prop="id" label="ID" width="60" align="center" sortable="custom" />

      <el-table-column label="岗位" width="130">
        <template #default="{ row }">
          <el-select
            v-model="row.jobName"
            size="small"
            :disabled="row.crmUserId != null"
            @change="emit('quick-update', row)"
          >
            <el-option v-for="j in jobs" :key="j" :label="j" :value="j" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column prop="candidateName" label="姓名" width="100" sortable="custom" />
      <el-table-column prop="mobile" label="手机号" width="120" />
      <el-table-column prop="interviewerName" label="面试负责人" width="110" />

      <el-table-column label="是否通过" width="120" align="center">
        <template #default="{ row }">
          <el-select
            v-model="row.passed"
            size="small"
            :disabled="row.crmUserId != null"
            @change="emit('quick-update', row)"
          >
            <el-option label="待确认" value="待确认" />
            <el-option label="是" value="是" />
            <el-option label="否" value="否" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column label="是否入职" width="120" align="center">
        <template #default="{ row }">
          <el-select
            v-model="row.hired"
            size="small"
            :disabled="row.crmUserId != null"
            @change="emit('quick-update', row)"
          >
            <el-option label="待确认" value="待确认" />
            <el-option label="是" value="是" />
            <el-option label="否" value="否" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column label="入职部门" width="140">
        <template #default="{ row }">
          <el-select
            v-model="row.deptId"
            size="small"
            placeholder="选择部门"
            :disabled="row.crmUserId != null"
            @change="emit('quick-update', row)"
          >
            <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column label="CRM 状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.crmSyncStatus === '已创建'" type="success" size="small">已创建</el-tag>
          <el-tag v-else-if="row.crmSyncStatus === '创建中'" type="warning" size="small">创建中</el-tag>
          <el-tooltip v-else-if="row.crmSyncStatus === '失败'" :content="row.crmError" placement="top">
            <el-tag type="danger" size="small" style="cursor: pointer;">失败 ⓘ</el-tag>
          </el-tooltip>
          <el-tag v-else type="info" size="small">未创建</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="简历" width="100" align="center">
        <template #default="{ row }">
          <PdfThumbnail
            v-if="row.resumePath"
            :path="row.resumePath"
            :row="row"
            @refresh="emit('refresh')"
          />
          <el-upload
            v-else
            :http-request="(options: any) => emit('row-upload', { options, row })"
            :show-file-list="false"
            accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.gif"
            drag
            class="row-upload"
          >
            <div class="add-resume">+</div>
          </el-upload>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="emit('edit', row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="emit('delete', row)">删除</el-button>
          <el-button
            size="small"
            type="warning"
            :disabled="row.crmUserId != null || row.crmSyncStatus === '创建中'"
            @click="emit('confirm-hire', row)"
          >
            {{ row.crmUserId ? '已创建' : '确认入职' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPageProxy"
        v-model:page-size="pageSizeProxy"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="emit('refresh')"
        @current-change="emit('refresh')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PdfThumbnail from './PdfThumbnail.vue'

const props = defineProps<{
  list: any[]
  jobs: string[]
  depts: any[]
  total: number
  currentPage: number
  pageSize: number
}>()

const emit = defineEmits<{
  (e: 'selection-change', rows: any[]): void
  (e: 'sort-change', payload: { prop: string; order: string }): void
  (e: 'quick-update', row: any): void
  (e: 'refresh'): void
  (e: 'edit', row: any): void
  (e: 'delete', row: any): void
  (e: 'confirm-hire', row: any): void
  (e: 'row-upload', payload: { options: any; row: any }): void
  (e: 'update:currentPage', v: number): void
  (e: 'update:pageSize', v: number): void
}>()

const tableRef = ref()

// currentPage / pageSize 双向绑定代理
const currentPageProxy = computed({
  get: () => props.currentPage,
  set: (v) => emit('update:currentPage', v)
})
const pageSizeProxy = computed({
  get: () => props.pageSize,
  set: (v) => emit('update:pageSize', v)
})

const handleSelectionChange = (rows: any[]) => {
  emit('selection-change', rows)
}

const handleSortChange = ({ prop, order }: any) => {
  emit('sort-change', { prop: prop || 'id', order: order || 'ascending' })
}

// 暴露 clearSelection 给主文件调用
defineExpose({
  clearSelection: () => tableRef.value?.clearSelection()
})
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.add-resume {
  width: 40px;
  height: 40px;
  border: 2px dashed #c0c4cc;
  border-radius: var(--app-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #c0c4cc;
  cursor: pointer;
  margin: 0 auto;
  transition: all 0.2s;
}

.add-resume:hover {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}

.row-upload :deep(.el-upload) {
  width: 100%;
  height: 100%;
}

.row-upload :deep(.el-upload-dragger) {
  padding: 0;
  border: none;
  background: transparent;
  width: 40px;
  height: 40px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.row-upload :deep(.el-upload-dragger:hover) {
  background: transparent;
}

:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #fafafa !important;
  color: #1f2d3d;
  font-weight: 600;
}

:deep(.el-table tr:hover > td) {
  background: #f5f7fa !important;
}
</style>