<template>
  <a-modal
    v-model:visible="visible"
    :title="title"
    :mask-closable="false"
    :esc-to-close="false"
    :width="width >= 960 ? 960 : '100%'"
    draggable
    @before-ok="save"
    @close="reset"
  >
    <div class="upload-modal-body">
      <div class="upload-left">
        <a-upload
          v-if="!uploaded"
          draggable
          :limit="1"
          accept=".zip,.tar.gz,.tgz,.7z"
          :auto-upload="false"
          tip="上传包含 meta.json 的模板压缩包（ZIP、TAR.GZ、7Z）"
          @change="handleFileChange"
        />
        <div v-else class="upload-success-hint">
          <icon-check-circle-fill style="color: rgb(var(--success-6)); font-size: 20px" />
          <span>{{ uploadedFileName }}</span>
          <a-button type="text" size="small" @click="resetUpload">重新上传</a-button>
        </div>

        <a-divider />

        <a-form ref="formRef" :model="form" :rules="rules" layout="vertical" size="small">
          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item label="模板名称" field="name">
                <a-input v-model="form.name" placeholder="模板名称" :max-length="100" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="GroupId" field="groupId">
                <a-input v-model="form.groupId" placeholder="com.example" :max-length="200" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="ArtifactId" field="artifactId">
                <a-input v-model="form.artifactId" placeholder="my-project" :max-length="200" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="版本号" field="version">
                <a-input v-model="form.version" placeholder="1.0.0" :max-length="50" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="作者" field="author">
                <a-input v-model="form.author" :max-length="100" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="图标文字" field="icon">
                <a-input v-model="form.icon" placeholder="如: SB" :max-length="10" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="描述" field="description">
                <a-tabs size="mini" default-active-key="edit" style="width: 100%">
                  <a-tab-pane key="edit" title="编辑">
                    <a-textarea v-model="form.description" :auto-size="{ minRows: 3, maxRows: 8 }" />
                  </a-tab-pane>
                  <a-tab-pane key="preview" title="预览">
                    <div class="md-preview" v-html="renderedDescription"></div>
                  </a-tab-pane>
                </a-tabs>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="标签">
                <div class="tags-editor">
                  <div class="tags-list">
                    <a-tag v-for="(tag, idx) in form.tags" :key="idx" :color="tag.color" closable @close="removeTag(idx)">
                      {{ tag.label }}
                    </a-tag>
                  </div>
                  <a-input-group>
                    <a-input v-model="newTagLabel" placeholder="输入标签" size="small" :max-length="50" @keypress.enter="addTag" />
                    <a-button type="primary" size="small" @click="addTag">添加</a-button>
                  </a-input-group>
                </div>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <a-divider direction="vertical" class="upload-divider" />

      <div class="upload-right">
        <div class="file-tree-header">
          <span class="file-tree-title">模板文件预览</span>
          <a-button v-if="previewContent" type="text" size="small" @click="previewContent = ''">
            <icon-arrow-left /> 返回
          </a-button>
        </div>
        <div v-if="previewContent" class="file-preview">
          <div class="file-preview-name">{{ previewFileName }}</div>
          <pre class="file-preview-code"><code>{{ previewContent }}</code></pre>
        </div>
        <div v-else-if="fileTree.length > 0" class="file-tree">
          <FileTreeItem
            v-for="treeNode in fileTree"
            :key="treeNode.path"
            :node="treeNode"
            :depth="0"
            @preview="onPreviewFile"
          />
        </div>
        <a-empty v-else description="上传 ZIP 后显示文件结构" />
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useWindowSize } from '@vueuse/core'
import { marked } from 'marked'
import {
  addTemplate,
  getTemplateDetail,
  listTemplateFiles,
  readTemplateFileContent,
  updateTemplate,
  uploadTemplateZip,
} from '@/apis/template'
import type { FileTreeNode } from '@/apis/template'

const emit = defineEmits<{
  (e: 'save-success'): void
}>()

const { width } = useWindowSize()

const dataId = ref(0)
const visible = ref(false)
const isUpdate = computed(() => !!dataId.value)
const title = computed(() => (isUpdate.value ? '修改模板' : '新增模板'))
const formRef = ref()

const uploaded = ref(false)
const uploadedFileName = ref('')
const pendingFile = ref<File | null>(null)
const fileTree = ref<FileTreeNode[]>([])
const previewContent = ref('')
const previewFileName = ref('')

interface TagItem {
  label: string
  color: string
}

const form = reactive({
  name: '',
  groupId: '',
  artifactId: '',
  icon: '',
  author: '',
  description: '',
  version: '',
  downloadUrl: '',
  searchRefId: '',
  tags: [] as TagItem[],
})

const TAG_COLORS = ['blue', 'green', 'purple', 'red', 'orange', 'cyan', 'gold', 'lime']
const newTagLabel = ref('')

const addTag = () => {
  const label = newTagLabel.value.trim()
  if (!label) return
  if (form.tags.some((t) => t.label === label)) return
  form.tags.push({ label, color: TAG_COLORS[form.tags.length % TAG_COLORS.length] })
  newTagLabel.value = ''
}

const removeTag = (index: number) => {
  form.tags.splice(index, 1)
}

const rules = {
  name: [{ required: true, message: '请输入模板名称' }],
  groupId: [{ required: true, message: '请输入 GroupId' }],
  artifactId: [{ required: true, message: '请输入 ArtifactId' }],
  version: [{ required: true, message: '请输入版本号' }],
}

const renderedDescription = computed(() => {
  if (!form.description) return ''
  return marked.parse(form.description) as string
})

const loadFileTree = async () => {
  if (!form.groupId || !form.artifactId || !form.version) return
  try {
    const res = await listTemplateFiles(form.groupId, form.artifactId, form.version)
    fileTree.value = res.data
  } catch {
    fileTree.value = []
  }
}

const doUpload = async () => {
  if (!pendingFile.value) return
  try {
    const res = await uploadTemplateZip(pendingFile.value)
    const data = res.data
    form.groupId = data.groupId || form.groupId || ''
    form.artifactId = data.artifactId || form.artifactId || ''
    form.version = data.version || form.version || ''
    form.name = data.name || form.name || ''
    form.description = data.description || form.description || ''
    form.downloadUrl = data.downloadUrl || ''
    uploaded.value = true
    Message.success('ZIP 解析成功')
    await loadFileTree()
  } catch (e: any) {
    Message.error(e?.message || '上传失败')
  }
}

const handleFileChange = async (fileList: any) => {
  const file = fileList[fileList.length - 1]
  if (!file?.file) return
  pendingFile.value = file.file
  uploadedFileName.value = file.file.name
  await doUpload()
}

const onPreviewFile = async (path: string, name: string) => {
  try {
    const res = await readTemplateFileContent(form.groupId, form.artifactId, form.version, path)
    previewContent.value = res.data
    previewFileName.value = name
  } catch {
    Message.error('无法预览该文件')
  }
}

const resetUpload = () => {
  uploaded.value = false
  uploadedFileName.value = ''
  pendingFile.value = null
  fileTree.value = []
  previewContent.value = ''
}

const reset = () => {
  formRef.value?.resetFields()
  Object.assign(form, { name: '', groupId: '', artifactId: '', icon: '', author: '', description: '', version: '', downloadUrl: '', searchRefId: '', tags: [] })
  resetUpload()
}

const save = async () => {
  try {
    const err = await formRef.value?.validate()
    if (err) return false
    if (!isUpdate.value && pendingFile.value && !form.downloadUrl) {
      await doUpload()
    }
    if (isUpdate.value) {
      await updateTemplate(dataId.value, form)
      Message.success('修改成功')
    } else {
      await addTemplate(form)
      Message.success('新增成功')
    }
    emit('save-success')
    return true
  } catch {
    return false
  }
}

const onAdd = () => {
  reset()
  dataId.value = 0
  visible.value = true
}

const onUpdate = async (id: number) => {
  reset()
  dataId.value = id
  const { data } = await getTemplateDetail(id)
  Object.assign(form, {
    name: data.name,
    groupId: data.groupId || '',
    artifactId: data.artifactId || '',
    icon: data.icon || '',
    author: data.author || '',
    description: data.description || '',
    version: data.version || '',
    downloadUrl: data.downloadUrl || '',
    searchRefId: data.searchRefId || '',
    tags: data.tags || [],
  })
  uploaded.value = true
  uploadedFileName.value = '已有模板文件'
  visible.value = true
  await loadFileTree()
}

const FileTreeItem = defineComponent({
  name: 'FileTreeItem',
  props: {
    node: { type: Object as () => FileTreeNode, required: true },
    depth: { type: Number, default: 0 },
  },
  emits: ['preview'],
  setup(props, { emit }) {
    const expanded = ref(true)
    const toggle = () => {
      expanded.value = !expanded.value
    }
    return () => {
      const { node, depth } = props
      const indent = { paddingLeft: `${depth * 16}px` }
      if (node.isDir) {
        const childNodes = expanded.value && node.children
          ? node.children.map((child: FileTreeNode) =>
            h(FileTreeItem, { node: child, depth: depth + 1, onPreview: (p: string, n: string) => emit('preview', p, n) }),
          )
          : null
        return h('div', {}, [
          h('div', {
            class: 'tree-node tree-dir',
            style: indent,
            onClick: toggle,
          }, [
            h('span', { class: 'tree-icon' }, expanded.value ? '📂' : '📁'),
            h('span', { class: 'tree-name' }, node.name),
          ]),
          childNodes,
        ])
      }
      return h('div', {
        class: 'tree-node tree-file',
        style: indent,
        onClick: () => emit('preview', node.path, node.name),
      }, [
        h('span', { class: 'tree-icon' }, '📄'),
        h('span', { class: 'tree-name' }, node.name),
        node.size != null ? h('span', { class: 'tree-size' }, formatSize(node.size)) : null,
      ])
    }
  },
})

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

defineExpose({ onAdd, onUpdate })
</script>

<style scoped lang="scss">
.upload-modal-body {
  display: flex;
  gap: 0;
  height: 520px;
}

.upload-left {
  flex: 0 0 380px;
  overflow-y: auto;
  padding-right: 16px;
}

.upload-divider {
  height: auto;
  margin: 0 !important;
}

.upload-right {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  padding-left: 16px;
  display: flex;
  flex-direction: column;
}

.upload-success-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: var(--color-fill-1);
  border-radius: 6px;
}

.file-tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.file-tree-title {
  font-weight: 600;
  font-size: 14px;
}

.md-preview {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  min-height: 60px;
  max-height: 180px;
  overflow-y: auto;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.tags-editor {
  width: 100%;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 6px;
}

.file-tree {
  font-size: 13px;
  flex: 1;
  overflow-y: auto;
}

:deep(.tree-node) {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;

  &:hover {
    background: var(--color-fill-2);
  }
}

:deep(.tree-icon) {
  font-size: 14px;
  flex-shrink: 0;
}

:deep(.tree-name) {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.tree-size) {
  font-size: 11px;
  color: var(--color-text-4);
  flex-shrink: 0;
}

.file-preview {
  height: 100%;
}

.file-preview-name {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  padding: 6px 10px;
  background: var(--color-fill-2);
  border-radius: 4px;
}

.file-preview-code {
  margin: 0;
  padding: 12px;
  background: var(--color-fill-1);
  border-radius: 6px;
  overflow: auto;
  max-height: 400px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;

  code {
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  }
}
</style>
