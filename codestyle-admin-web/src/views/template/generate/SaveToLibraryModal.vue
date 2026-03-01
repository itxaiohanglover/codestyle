<template>
  <a-modal v-model:visible="visible" title="保存到模板库" :width="520" :mask-closable="false" @before-ok="onSave" @close="reset">
    <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
      <a-form-item label="模板名称" field="name">
        <a-input v-model="form.name" placeholder="为代码片段命名" :max-length="100" />
      </a-form-item>
      <a-form-item label="描述" field="description">
        <a-textarea v-model="form.description" placeholder="简要描述该代码片段的用途" :auto-size="{ minRows: 2, maxRows: 6 }" :max-length="500" />
      </a-form-item>
      <a-form-item label="标签">
        <div class="tags-editor">
          <div v-if="form.tags.length" class="tags-list">
            <a-tag v-for="(tag, idx) in form.tags" :key="idx" :color="tag.color" closable @close="form.tags.splice(idx, 1)">
              {{ tag.label }}
            </a-tag>
          </div>
          <a-input-group>
            <a-input v-model="newTag" placeholder="输入标签后回车" size="small" :max-length="30" @keypress.enter="addTag" />
            <a-button type="primary" size="small" @click="addTag">添加</a-button>
          </a-input-group>
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { saveSnippetToLibrary } from '@/apis/template/generate'
import type { TagItem } from '@/apis/template/type'

const visible = ref(false)
const snippetId = ref<number>(0)
const formRef = ref()
const newTag = ref('')
const TAG_COLORS = ['blue', 'green', 'purple', 'red', 'orange', 'cyan', 'gold', 'lime']

const form = reactive<{ name: string, description: string, tags: TagItem[] }>({
  name: '',
  description: '',
  tags: [],
})

const rules = {
  name: [{ required: true, message: '请输入名称' }],
}

function addTag() {
  const label = newTag.value.trim()
  if (!label || form.tags.some((t) => t.label === label)) return
  form.tags.push({ label, color: TAG_COLORS[form.tags.length % TAG_COLORS.length] })
  newTag.value = ''
}

async function onSave() {
  const err = await formRef.value?.validate()
  if (err) return false
  try {
    await saveSnippetToLibrary(snippetId.value, { name: form.name, description: form.description, tags: form.tags })
    Message.success('已保存到模板库')
    return true
  } catch {
    Message.error('保存失败')
    return false
  }
}

function reset() {
  formRef.value?.resetFields()
  Object.assign(form, { name: '', description: '', tags: [] })
  newTag.value = ''
}

function open(id: number) {
  snippetId.value = id
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.tags-editor { width: 100%; }
.tags-list { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 6px; }
</style>
