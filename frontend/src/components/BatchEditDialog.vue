<template>
  <el-dialog v-model="visible" title="批量修改" width="400px">
    <el-form label-width="100px">
      <el-form-item label="是否通过">
        <el-select v-model="form.passed" placeholder="不变" clearable style="width: 100%;">
          <el-option label="是" value="是" />
          <el-option label="否" value="否" />
          <el-option label="待确认" value="待确认" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否入职">
        <el-select v-model="form.hired" placeholder="不变" clearable style="width: 100%;">
          <el-option label="是" value="是" />
          <el-option label="否" value="否" />
          <el-option label="待确认" value="待确认" />
        </el-select>
      </el-form-item>
      <el-form-item label="入职部门">
        <el-select v-model="form.deptId" placeholder="不变" clearable style="width: 100%;">
          <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

const props = defineProps<{
  modelValue: boolean
  depts: any[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'confirm', form: { passed: string; hired: string; deptId: number | null }): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const form = ref<{ passed: string; hired: string; deptId: number | null }>({
  passed: '',
  hired: '',
  deptId: null
})

// 每次打开弹窗时重置表单
watch(() => props.modelValue, (v) => {
  if (v) {
    form.value = { passed: '', hired: '', deptId: null }
  }
})

const handleConfirm = () => {
  emit('confirm', form.value)
}
</script>