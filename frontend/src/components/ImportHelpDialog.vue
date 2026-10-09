<template>
  <el-dialog v-model="visible" title="Excel 导入说明" width="600px">
    <div class="import-help">
      <h3>1. 文件格式</h3>
      <p>只支持 <b>.xlsx</b> 或 <b>.xls</b> 格式的 Excel 文件。</p>

      <h3>2. 表头（第一行）必须完全匹配</h3>
      <p>列名必须和下面完全一致，不能写错字：</p>
      <el-table :data="headers" border size="small" style="margin: 10px 0;">
        <el-table-column prop="name" label="列名" width="150" />
        <el-table-column prop="required" label="必填" width="80" />
        <el-table-column prop="example" label="示例" />
      </el-table>

      <h3>3. 注意事项</h3>
      <ul>
        <li>不要加额外的列，多了会报错</li>
        <li>空行会被跳过，但最好删掉</li>
        <li>手机号不能重复，重复的会被跳过，计入“失败”</li>
        <li>ID 列会被忽略，导入时自动生成</li>
      </ul>

      <h3>4. 怎么拿到模板</h3>
      <p>点“导出 Excel”，导出的文件就是标准格式。删掉里面的数据行，保留表头，填入你的数据，再上传即可。</p>
    </div>
    <template #footer>
      <el-button type="primary" @click="visible = false">知道了</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: boolean): void }>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const headers = [
  { name: '岗位', required: '是', example: '一线销售' },
  { name: '姓名', required: '是', example: '张三' },
  { name: '手机号', required: '是', example: '13811111111' },
  { name: '面试负责人', required: '是', example: '李经理' },
  { name: '是否通过', required: '否', example: '是 / 否 / 待确认' },
  { name: '是否入职', required: '否', example: '是 / 否 / 待确认' },
  { name: '入职部门', required: '否', example: '一线销售' }
]
</script>

<style scoped>
.import-help h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
  margin: 16px 0 8px 0;
}
.import-help p {
  font-size: 14px;
  color: #606266;
  margin: 0 0 8px 0;
}
.import-help ul {
  font-size: 14px;
  color: #606266;
  padding-left: 20px;
  margin: 0;
}
.import-help ul li {
  margin-bottom: 4px;
}
</style>