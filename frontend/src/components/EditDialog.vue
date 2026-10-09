<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="520px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" label-width="100px" label-position="right">
      <el-form-item label="岗位" required>
        <el-select v-model="form.jobName" placeholder="选择岗位" style="width: 100%;">
          <el-option v-for="j in jobs" :key="j" :label="j" :value="j" />
        </el-select>
      </el-form-item>
      <el-form-item label="面试人姓名" required>
        <el-input v-model="form.candidateName" placeholder="面试人姓名" />
      </el-form-item>
      <el-form-item label="手机号" required>
        <el-input v-model="form.mobile" placeholder="11 位手机号" />
      </el-form-item>
      <el-form-item label="面试负责人" required>
        <el-input v-model="form.interviewerName" placeholder="面试负责人" />
      </el-form-item>
      <el-form-item label="是否通过">
        <el-radio-group v-model="form.passed">
          <el-radio label="待确认">待确认</el-radio>
          <el-radio label="是">是</el-radio>
          <el-radio label="否">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否入职">
        <el-radio-group v-model="form.hired">
          <el-radio label="待确认">待确认</el-radio>
          <el-radio label="是">是</el-radio>
          <el-radio label="否">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="入职部门">
        <el-select v-model="form.deptId" placeholder="选择部门" style="width: 100%;">
          <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: boolean
  form: any
  title: string
  jobs: string[]
  depts: any[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'update:form', v: any): void
  (e: 'submit'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const form = computed({
  get: () => props.form,
  set: (v) => emit('update:form', v)
})

const handleSubmit = () => {
  emit('submit')
}
</script>