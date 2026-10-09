<template>
  <div class="card search-card">
    <el-form :inline="true" style="margin: 0;">
      <el-form-item label="搜索">
        <el-input
          v-model="search.keyword"
          placeholder="姓名 / 手机号 / 岗位"
          clearable
          style="width: 220px;"
          @keyup.enter="emit('search')"
        />
      </el-form-item>
      <el-form-item label="是否通过">
        <el-select
          v-model="search.passed"
          placeholder="全部"
          clearable
          style="width: 110px;"
          @change="emit('search')"
        >
          <el-option label="是" value="是" />
          <el-option label="否" value="否" />
          <el-option label="待确认" value="待确认" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否入职">
        <el-select
          v-model="search.hired"
          placeholder="全部"
          clearable
          style="width: 110px;"
          @change="emit('search')"
        >
          <el-option label="是" value="是" />
          <el-option label="否" value="否" />
          <el-option label="待确认" value="待确认" />
        </el-select>
      </el-form-item>
      <el-form-item label="入职部门">
        <el-select
          v-model="search.deptId"
          placeholder="全部"
          clearable
          style="width: 130px;"
          @change="emit('search')"
        >
          <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="emit('search')">查询</el-button>
        <el-button @click="emit('reset')">重置</el-button>
        <el-button type="success" @click="emit('add')">+ 新增</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" style="margin: 10px 0 0 0;">
      <el-form-item>
        <el-button
          type="danger"
          :disabled="selectedCount === 0"
          @click="emit('batch-delete')"
        >
          批量删除（{{ selectedCount }}）
        </el-button>
        <el-button
          type="warning"
          :disabled="selectedCount === 0"
          @click="emit('batch-edit')"
        >
          批量修改（{{ selectedCount }}）
        </el-button>
        <el-button type="primary" plain @click="emit('export')">
          导出 Excel（{{ selectedCount > 0 ? '选中 ' + selectedCount + ' 条' : '全部' }}）
        </el-button>
        <el-upload
          :http-request="(options: any) => emit('import', options)"
          :show-file-list="false"
          accept=".xlsx,.xls"
          style="display: inline-block; margin-left: 10px;"
        >
          <el-button type="success" plain>导入 Excel</el-button>
        </el-upload>
        <el-button type="info" plain @click="emit('import-help')" style="margin-left: 10px;">
          导入说明
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  search: { keyword: string; passed: string; hired: string; deptId: number | null }
  depts: any[]
  selectedCount: number
}>()

const emit = defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
  (e: 'add'): void
  (e: 'batch-delete'): void
  (e: 'batch-edit'): void
  (e: 'export'): void
  (e: 'import', options: any): void
  (e: 'import-help'): void
}>()
</script>