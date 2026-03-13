<template>
  <a-modal v-model:visible="visible" width="90%" :footer="false" unmount-on-close>
    <template #title>
      <div class="modal-title">
        <div class="modal-icon" :style="iconStyle">{{ templateData?.icon }}</div>
        <span>{{ templateData?.name }}</span>
      </div>
    </template>
    <div class="preview-content">
      <a-layout :has-sider="true">
        <!-- 项目信息侧边栏 -->
        <a-layout-sider :width="420" :resize-directions="['right']" style="min-width: 300px; max-width: 560px">
          <a-scrollbar style="height: 700px; overflow: auto">
            <div class="project-info">
              <!-- 项目标题 -->
              <div class="project-title-section">
                <h2 class="project-main-title">{{ templateData?.name }} 模板</h2>
                <div class="project-tags">
                  <a-tag v-for="tag in templateData?.tags" :key="tag.label" :color="tag.color" size="small">
                    {{ tag.label }}
                  </a-tag>
                </div>
              </div>

              <!-- 项目简介 -->
              <div class="info-section">
                <div class="section-title">项目简介</div>
                <div class="section-text md-preview" v-html="renderedDescription"></div>
              </div>

              <!-- 统计数据 -->
              <div class="stats-grid">
                <div class="stat-card">
                  <icon-star-fill class="stat-icon" style="color: rgb(var(--warning-6))" />
                  <div class="stat-value">{{ templateData?.starCount ?? 0 }}</div>
                  <div class="stat-label">收藏数</div>
                </div>
                <div class="stat-card">
                  <icon-download class="stat-icon" style="color: rgb(var(--primary-6))" />
                  <div class="stat-value">{{ templateData?.downloadCount }}</div>
                  <div class="stat-label">下载量</div>
                </div>
              </div>

              <!-- 版本信息 -->
              <div class="version-section">
                <span class="version-label">版本</span>
                <a-select v-if="versionList.length > 1" v-model="currentVersionId" size="small" style="width: 140px" @change="onVersionChange">
                  <a-option v-for="v in versionList" :key="v.id" :value="v.id" :label="v.version" />
                </a-select>
                <span v-else class="version-number">{{ templateData?.version || '-' }}</span>
              </div>

              <!-- 元数据 -->
              <div class="meta-section">
                <div class="meta-item">
                  <div class="meta-icon"><icon-user /></div>
                  <div class="meta-content">
                    <div class="meta-label">创建者</div>
                    <div class="meta-value">{{ templateData?.createUserString || templateData?.author }}</div>
                  </div>
                </div>
                <div class="meta-item">
                  <div class="meta-icon"><icon-clock-circle /></div>
                  <div class="meta-content">
                    <div class="meta-label">创建时间</div>
                    <div class="meta-value">{{ templateData?.createTime || '-' }}</div>
                  </div>
                </div>
                <div class="meta-item">
                  <div class="meta-icon"><icon-calendar /></div>
                  <div class="meta-content">
                    <div class="meta-label">更新时间</div>
                    <div class="meta-value">{{ templateData?.updateTime || '-' }}</div>
                  </div>
                </div>
              </div>

              <!-- 下载按钮 -->
              <a-button type="primary" long @click="onDownload">
                <template #icon><icon-download /></template>
                下载模板
              </a-button>

              <div v-if="templateData?.groupId || templateData?.artifactId" class="info-section">
                <div class="section-title">坐标信息</div>
                <div class="section-text">
                  <div v-if="templateData?.groupId">GroupId: {{ templateData.groupId }}</div>
                  <div v-if="templateData?.artifactId">ArtifactId: {{ templateData.artifactId }}</div>
                </div>
              </div>
            </div>
          </a-scrollbar>
        </a-layout-sider>

        <!-- 文件树 + 预览区 -->
        <a-layout-content>
          <a-card :bordered="false">
            <template #title>
              <div class="content-header">
                <a-typography-title :heading="6" style="margin: 0">
                  {{ filePreviewContent ? filePreviewName : '模板文件' }}
                </a-typography-title>
                <div class="content-actions">
                  <a-button v-if="filePreviewContent" type="text" size="small" @click="filePreviewContent = ''">
                    <icon-arrow-left /> 返回文件树
                  </a-button>
                  <a-link @click="onCopy">
                    <template #icon><icon-copy /></template>
                    复制下载链接
                  </a-link>
                </div>
              </div>
            </template>
            <a-scrollbar style="height: 650px; overflow: auto">
              <div v-if="filePreviewContent" class="file-preview-area">
                <GiCodeView :type="getLangType(filePreviewName)" :code-json="filePreviewContent" />
              </div>
              <div v-else-if="fileTreeData.length > 0" class="file-tree-area">
                <div class="tree-search-bar" style="padding: 8px 12px;">
                  <a-input v-model="treeSearchKey" placeholder="搜索文件..." allow-clear size="small">
                    <template #prefix><icon-search /></template>
                  </a-input>
                </div>
                <div
                  v-for="node in visibleFlatTree"
                  :key="node.path"
                  class="tree-row"
                  :class="{ 'tree-row-dir': node.isDir, 'tree-row-file': !node.isDir }"
                  :style="{ paddingLeft: `${node.depth * 20 + 12}px` }"
                  @click="node.isDir ? toggleDir(node.path) : onFileClick(node.path, node.name)"
                >
                  <span v-if="node.isDir" class="tree-row-icon">{{ collapsedDirs.has(node.path) ? '📁' : '📂' }}</span>
                  <span v-else class="tree-row-icon">📄</span>
                  <span class="tree-row-name">{{ node.name }}</span>
                  <span v-if="!node.isDir && node.size != null" class="tree-row-size">{{ formatSize(node.size) }}</span>
                </div>
              </div>
              <div v-else class="template-description">
                <a-spin v-if="fileTreeLoading" />
                <p v-else>{{ templateData?.description }}</p>
              </div>
            </a-scrollbar>
          </a-card>
        </a-layout-content>
      </a-layout>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import axios from 'axios'
import { Message } from '@arco-design/web-vue'
import { useClipboard } from '@vueuse/core'
import { marked } from 'marked'
import GiCodeView from '@/components/GiCodeView/index.vue'
import type { FileTreeNode, TemplateDetail, TemplateItem } from '@/apis/template'
import { downloadTemplate, getTemplateDetail, listTemplateFiles, listVersions, readTemplateFileContent, recordDownload } from '@/apis/template'

const { copy, copied } = useClipboard()

const visible = ref(false)
const templateData = ref<TemplateDetail>()

const renderedDescription = computed(() => {
  if (!templateData.value?.description) return ''
  return marked.parse(templateData.value.description) as string
})
const iconStyle = ref<Record<string, string>>({})
const detailLoading = ref(false)
const fileTreeData = ref<FileTreeNode[]>([])
const fileTreeLoading = ref(false)
const filePreviewContent = ref('')
const filePreviewName = ref('')
const collapsedDirs = ref(new Set<string>())
const versionList = ref<TemplateItem[]>([])
const currentVersionId = ref<number>()
const treeSearchKey = ref('')

const EXT_LANG_MAP: Record<string, string> = {
  '.java': 'java',
  '.xml': 'xml',
  '.pom': 'xml',
  '.json': 'json',
  '.yml': 'yaml',
  '.yaml': 'yaml',
  '.sql': 'sql',
  '.html': 'html',
  '.htm': 'html',
  '.css': 'css',
  '.scss': 'css',
  '.less': 'css',
  '.py': 'python',
  '.md': 'markdown',
  '.vue': 'vue',
  '.js': 'javascript',
  '.jsx': 'javascript',
  '.ts': 'typescript',
  '.tsx': 'typescript',
}
function getLangType(name: string): string {
  const ext = name.includes('.') ? `.${name.split('.').pop()!.toLowerCase()}` : ''
  return EXT_LANG_MAP[ext] || 'text'
}

// 图标渐变色
const iconGradients = [
  'linear-gradient(135deg, #0891B2 0%, #22D3EE 100%)',
  'linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)',
  'linear-gradient(135deg, #059669 0%, #34D399 100%)',
  'linear-gradient(135deg, #DC2626 0%, #F87171 100%)',
  'linear-gradient(135deg, #D97706 0%, #FBBF24 100%)',
  'linear-gradient(135deg, #2563EB 0%, #60A5FA 100%)',
]

const onCopy = () => {
  if (templateData.value?.downloadUrl) {
    const base = import.meta.env.VITE_API_BASE_URL || window.location.origin
    copy(`${base}${templateData.value.downloadUrl}`)
  }
}

watch(copied, () => {
  if (copied.value) {
    Message.success('复制成功')
  }
})

const onDownload = async () => {
  const d = templateData.value
  if (!d) {
    console.log('[Download] templateData is null')
    return
  }
  console.log('[Download] Starting download:', JSON.parse(JSON.stringify(d)))
  try {
    console.log('[Download] Step 1: Recording download count for id:', d.id)
    await recordDownload(d.id)
    console.log('[Download] Step 2: Download recorded, checking params - groupId:', d.groupId, 'artifactId:', d.artifactId, 'version:', d.version)
    if (d.groupId && d.artifactId && d.version) {
      console.log('[Download] Step 3: Calling downloadTemplate API with params:', d.groupId, d.artifactId, d.version)
      // 直接用 axios 测试，不通过封装
      const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000'
      const token = localStorage.getItem('token')
      console.log('[Download] Token exists:', !!token, 'Token:', token ? token.substring(0, 20) + '...' : null)
      const res = await axios.get(`${baseURL}/open-api/template/download`, {
        params: { groupId: d.groupId, artifactId: d.artifactId, version: d.version },
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : {}
      })
      console.log('[Download] Step 4: API response status:', res.status, res.statusText)
      console.log('[Download] Step 5: Creating blob and download link')
      const url = URL.createObjectURL(new Blob([res.data]))
      const a = document.createElement('a')
      a.href = url
      a.download = `${d.name || d.artifactId}-${d.version}.zip`
      a.click()
      URL.revokeObjectURL(url)
      console.log('[Download] Step 6: Download completed')
    } else if (d.downloadUrl) {
      console.log('[Download] Using downloadUrl fallback:', d.downloadUrl)
      const base = import.meta.env.VITE_API_BASE_URL || ''
      window.open(`${base}${d.downloadUrl}`, '_blank')
    }
    Message.success('下载成功')
  } catch (err: any) {
    console.error('[Download] Error:', err)
    console.error('[Download] Error status:', err?.response?.status)
    console.error('[Download] Error statusText:', err?.response?.statusText)
    console.error('[Download] Error data:', err?.response?.data)
    // 尝试读取 blob 错误信息
    if (err?.response?.data instanceof Blob) {
      const text = await err.response.data.text()
      console.error('[Download] Blob error text:', text)
      try {
        const json = JSON.parse(text)
        console.error('[Download] Blob error JSON:', json)
      } catch {}
    }
    Message.error('下载失败: ' + (err?.response?.statusText || err?.message || '未知错误'))
  }
}

const loadFileTree = async () => {
  const d = templateData.value
  if (!d?.groupId || !d?.artifactId || !d?.version) return
  fileTreeLoading.value = true
  try {
    const res = await listTemplateFiles(d.groupId, d.artifactId, d.version)
    fileTreeData.value = res.data
  } catch {
    fileTreeData.value = []
  } finally {
    fileTreeLoading.value = false
  }
}

const loadVersions = async () => {
  const d = templateData.value
  if (!d?.groupId || !d?.artifactId) return
  try {
    const res = await listVersions(d.groupId, d.artifactId)
    versionList.value = res.data
  } catch {
    versionList.value = []
  }
}

const onVersionChange = async (id: number) => {
  try {
    const res = await getTemplateDetail(id)
    templateData.value = res.data
    filePreviewContent.value = ''
    collapsedDirs.value = new Set()
    loadFileTree()
  } catch {
    Message.error('加载版本详情失败')
  }
}

const onOpen = async (item: TemplateItem) => {
  templateData.value = item as TemplateDetail
  let hash = 0
  for (let i = 0; i < item.icon.length; i++) {
    hash = item.icon.charCodeAt(i) + ((hash << 5) - hash)
  }
  const index = Math.abs(hash) % iconGradients.length
  iconStyle.value = { background: iconGradients[index] }

  visible.value = true
  detailLoading.value = true

  try {
    const res = await getTemplateDetail(item.id)
    templateData.value = res.data
    currentVersionId.value = item.id
    loadFileTree()
    loadVersions()
  } catch {
    Message.error('获取模板详情失败')
  } finally {
    detailLoading.value = false
  }
}

const onFileClick = async (path: string, name: string) => {
  const d = templateData.value
  if (!d?.groupId || !d?.artifactId || !d?.version) return
  try {
    const res = await readTemplateFileContent(d.groupId, d.artifactId, d.version, path)
    filePreviewContent.value = res.data
    filePreviewName.value = name
  } catch {
    Message.error('无法预览该文件')
  }
}

interface FlatNode {
  name: string
  path: string
  isDir: boolean
  size?: number
  depth: number
}

function flattenTree(nodes: FileTreeNode[], depth: number, collapsed: Set<string>): FlatNode[] {
  const result: FlatNode[] = []
  for (const n of nodes) {
    result.push({ name: n.name, path: n.path, isDir: n.isDir, size: n.size, depth })
    if (n.isDir && n.children && !collapsed.has(n.path)) {
      result.push(...flattenTree(n.children, depth + 1, collapsed))
    }
  }
  return result
}

const visibleFlatTree = computed(() => {
  const list = flattenTree(fileTreeData.value, 0, collapsedDirs.value)
  const key = treeSearchKey.value.trim().toLowerCase()
  if (!key) return list
  const matchPaths = new Set<string>()
  for (const n of list) {
    if (!n.isDir && n.name.toLowerCase().includes(key)) {
      matchPaths.add(n.path)
      let parent = n.path.substring(0, n.path.lastIndexOf('/'))
      while (parent) {
        matchPaths.add(parent)
        parent = parent.substring(0, parent.lastIndexOf('/'))
      }
    }
  }
  return list.filter((n) => matchPaths.has(n.path))
})

const toggleDir = (path: string) => {
  const s = new Set(collapsedDirs.value)
  if (s.has(path)) s.delete(path)
  else s.add(path)
  collapsedDirs.value = s
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

defineExpose({ onOpen })
</script>

<style scoped lang="scss">
.modal-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.modal-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.preview-content {
  :deep(.arco-layout-sider) {
    min-width: 200px;
    white-space: nowrap;
    border-right: 1px solid var(--color-border);
  }
  :deep(.arco-layout-content) {
    border-left: none;
  }
}

// 项目信息侧边栏
.project-info {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.project-title-section {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.project-main-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0 0 12px;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.section-text {
  font-size: 13px;
  line-height: 1.8;
  color: var(--color-text-2);
  word-break: break-word;
  white-space: pre-wrap;
}

.md-preview {
  white-space: normal;
  :deep(h1), :deep(h2), :deep(h3) {
    margin: 8px 0 4px;
    font-size: 14px;
    font-weight: 600;
  }
  :deep(p) {
    margin: 4px 0;
  }
  :deep(ul), :deep(ol) {
    padding-left: 18px;
    margin: 4px 0;
  }
  :deep(code) {
    padding: 1px 4px;
    background: var(--color-fill-2);
    border-radius: 3px;
    font-size: 12px;
  }
  :deep(pre) {
    padding: 8px;
    background: var(--color-fill-1);
    border-radius: 4px;
    overflow-x: auto;
    margin: 4px 0;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-card {
  padding: 16px;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-icon {
  font-size: 24px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-1);
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-3);
}

.version-section {
  padding: 12px 16px;
  border-radius: 6px;
  background: var(--color-fill-1);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.version-label {
  font-size: 12px;
  color: var(--color-text-3);
  font-weight: 500;
}

.version-number {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
  font-family: monospace;
}

.meta-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-2);
}

.meta-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-fill-2);
  color: var(--color-text-2);
  font-size: 16px;
  flex-shrink: 0;
}

.meta-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.meta-label {
  font-size: 11px;
  color: var(--color-text-3);
  font-weight: 500;
}

.meta-value {
  font-size: 13px;
  color: var(--color-text-1);
  font-weight: 600;
}

// 文件树
.file-tree-header {
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-2);
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.content-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-tree-area {
  padding: 8px 0;
}

.tree-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  user-select: none;

  &:hover {
    background: var(--color-fill-2);
  }
}

.tree-row-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.tree-row-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-row-size {
  font-size: 11px;
  color: var(--color-text-4);
  flex-shrink: 0;
}

.file-preview-area {
  padding: 0;
}

.preview-code {
  margin: 0;
  padding: 16px;
  background: var(--color-fill-1);
  border-radius: 8px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;

  code {
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  }
}
</style>
