<template>
  <CodeMirror
    :model-value="codeValue"
    :tab-size="2"
    :basic="true"
    :dark="isDark"
    :readonly="!editable"
    :extensions="extensions"
    @update:model-value="onInput"
  />
</template>

<script setup lang="ts">
import CodeMirror from 'vue-codemirror6'
import { javascript } from '@codemirror/lang-javascript'
import { vue } from '@codemirror/lang-vue'
import { java } from '@codemirror/lang-java'
import { xml } from '@codemirror/lang-xml'
import { json } from '@codemirror/lang-json'
import { yaml } from '@codemirror/lang-yaml'
import { sql } from '@codemirror/lang-sql'
import { html } from '@codemirror/lang-html'
import { css } from '@codemirror/lang-css'
import { python } from '@codemirror/lang-python'
import { markdown } from '@codemirror/lang-markdown'
import { githubLight } from '@ddietr/codemirror-themes/github-light'
import { oneDark } from '@codemirror/theme-one-dark'
import { useAppStore } from '@/stores'

type LangType = 'javascript' | 'typescript' | 'vue' | 'java' | 'xml' | 'json' | 'yaml' | 'yml' | 'sql' | 'html' | 'css' | 'python' | 'markdown' | 'md' | 'text'

const props = withDefaults(defineProps<{
  type?: LangType
  codeJson: string
  editable?: boolean
}>(), {
  type: 'javascript',
  codeJson: '',
  editable: false,
})

const emit = defineEmits<{ (e: 'update:codeJson', v: string): void }>()

const appStore = useAppStore()
const isDark = computed(() => appStore.theme === 'dark')
const codeValue = computed(() => props.codeJson)

const langMap: Record<string, () => any> = {
  javascript: () => javascript(),
  typescript: () => javascript({ typescript: true }),
  vue: () => vue(),
  java: () => java(),
  xml: () => xml(),
  json: () => json(),
  yaml: () => yaml(),
  yml: () => yaml(),
  sql: () => sql(),
  html: () => html(),
  css: () => css(),
  python: () => python(),
  markdown: () => markdown(),
  md: () => markdown(),
}

const extensions = computed(() => {
  const arr: any[] = [isDark.value ? oneDark : githubLight]
  const factory = langMap[props.type]
  if (factory) arr.push(factory())
  return arr
})

function onInput(val: string | undefined) {
  if (props.editable && val !== undefined) emit('update:codeJson', val)
}
</script>

<style scoped lang="scss">
:deep(.ͼ1 .cm-scroller) {
  font-family: source-code-pro, Menlo, Monaco, Consolas, Courier New, monospace;
}
</style>
