<template>
  <div class="pdf-thumb" @click="openPreview">
    <canvas v-if="type === 'pdf'" ref="canvasRef"></canvas>
    <img v-else-if="type === 'image'" :src="blobUrl" class="thumb-img" />
    <div v-else class="file-icon">📄</div>
  </div>

  <el-dialog
    v-model="dialogVisible"
    title="预览"
    destroy-on-close
    :close-on-click-modal="true"
    width="450px"
    top="8vh"
    class="preview-dialog"
    :modal="true"
    append-to-body
  >
    <div class="preview-content">
      <canvas v-if="type === 'pdf'" ref="bigCanvasRef"></canvas>
      <img v-else-if="type === 'image'" :src="blobUrl" class="preview-img" />
    </div>

    <template #footer>
      <el-button type="primary" @click="handleDownload">下载</el-button>
      <el-button type="danger" @click="handleDeleteResume">删除简历</el-button>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as pdfjsLib from 'pdfjs-dist'
import workerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url'
import request from '../api/request'

pdfjsLib.GlobalWorkerOptions.workerSrc = workerUrl

const props = defineProps<{
  path: string
  row: any
}>()

const emit = defineEmits(['refresh'])

const canvasRef = ref<HTMLCanvasElement>()
const bigCanvasRef = ref<HTMLCanvasElement>()
const dialogVisible = ref(false)
const blobUrl = ref('')
const type = ref<'pdf' | 'image' | 'other'>('other')
let pdfDoc: any = null

const loadFile = async () => {
  try {
    const token = localStorage.getItem('token')
    const url = `http://localhost:8080/api/recruitment/download?path=${encodeURIComponent(props.path)}`
    const res = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const blob = await res.blob()
    const objectUrl = URL.createObjectURL(blob)
    blobUrl.value = objectUrl

    const lower = props.path.toLowerCase()
    if (lower.endsWith('.pdf')) {
      type.value = 'pdf'
      pdfDoc = await pdfjsLib.getDocument({ url: objectUrl }).promise
      await renderThumb()
    } else if (lower.endsWith('.jpg') || lower.endsWith('.jpeg') || lower.endsWith('.png') || lower.endsWith('.gif')) {
      type.value = 'image'
    } else {
      type.value = 'other'
    }
  } catch (e) {
    console.error(e)
  }
}

const renderThumb = async () => {
  if (!pdfDoc) return
  await nextTick()
  const page = await pdfDoc.getPage(1)
  const viewport = page.getViewport({ scale: 0.3 })
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')!
  canvas.width = viewport.width
  canvas.height = viewport.height
  canvas.style.width = viewport.width + 'px'
  canvas.style.height = viewport.height + 'px'
  await page.render({ canvasContext: ctx, viewport }).promise
}

const openPreview = async () => {
  if (type.value === 'other') return
  if (!pdfDoc && type.value === 'pdf') {
    ElMessage.error('PDF 加载失败，无法预览')
    return
  }
  dialogVisible.value = true

  if (type.value === 'pdf') {
    await nextTick()
    const page = await pdfDoc.getPage(1)
    const viewport = page.getViewport({ scale: 1.5 })
    const canvas = bigCanvasRef.value
    if (!canvas) return
    const ctx = canvas.getContext('2d')!
    canvas.width = viewport.width
    canvas.height = viewport.height
    canvas.style.width = '100%'
    canvas.style.height = 'auto'
    await page.render({ canvasContext: ctx, viewport }).promise
  }
}

const handleDownload = async () => {
  try {
    const token = localStorage.getItem('token')
    const url = `http://localhost:8080/api/recruitment/download?path=${encodeURIComponent(props.path)}`
    const res = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const blob = await res.blob()
    const fileName = props.path.split('/').pop() || 'resume'
    const objectUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = fileName
    link.click()
    URL.revokeObjectURL(objectUrl)
    ElMessage.success('下载成功')
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

const handleDeleteResume = async () => {
  try {
    await ElMessageBox.confirm('确定删除这份简历？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 1. 删物理文件
    await request.delete('/recruitment/resume', {
      params: { path: props.row.resumePath }
    })

    // 2. 清数据库路径（用专门接口，强制设 null）
    await request.delete(`/recruitment/${props.row.id}/resume-path`)

    ElMessage.success('已删除')
    dialogVisible.value = false

    // 3. 延迟刷新，让 fetchList 拿到新数据
    setTimeout(() => {
      emit('refresh')
    }, 300)
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(loadFile)
</script>

<style scoped>
.pdf-thumb {
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 40px;
}

.pdf-thumb canvas,
.thumb-img {
  max-width: 60px;
  max-height: 60px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.pdf-thumb canvas:hover,
.thumb-img:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.file-icon {
  font-size: 24px;
}

.preview-content {
  max-height: 70vh;
  overflow-y: auto;
  display: flex;
  justify-content: center;
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.preview-content canvas,
.preview-img {
  max-width: 100%;
  height: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: #fff;
}
</style>