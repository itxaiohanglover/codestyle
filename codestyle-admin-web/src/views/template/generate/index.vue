<template>
  <div class="gi_page generate-page">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <a-space>
          <a-button type="primary" @click="dataSourceDrawerVisible = true">
            <template #icon><icon-import /></template>
            数据源
          </a-button>
          <a-button @click="handleOpenWiki" :disabled="wikiTreeData.length === 0">
            <template #icon><icon-book /></template>
            Wiki 文档
          </a-button>
          <a-button @click="handleOpenCodePreview" :disabled="codeSnippets.length === 0">
            <template #icon><icon-code-block /></template>
            代码片段
            <a-badge v-if="codeSnippets.length > 0" :count="codeSnippets.length" :dot-style="{ marginLeft: '4px' }" />
          </a-button>
        </a-space>
      </div>
      <div class="toolbar-right">
        <a-button type="text" size="mini" @click="handleClearChat">
          <template #icon><icon-delete /></template>
          清空对话
        </a-button>
      </div>
    </div>

    <!-- 主内容区 (侧边栏 + 对话) -->
    <div class="main-content">
      <!-- 对话历史侧边栏 -->
      <div class="history-sidebar" :class="{ collapsed: isSidebarCollapsed }">
        <div class="sidebar-header">
          <template v-if="!isSidebarCollapsed">
            <span class="sidebar-title">对话历史</span>
          </template>
          <a-button
            type="text"
            size="mini"
            class="sidebar-toggle-btn"
            @click="toggleSidebar"
          >
            <template #icon>
              <icon-menu-fold v-if="!isSidebarCollapsed" />
              <icon-menu-unfold v-else />
            </template>
          </a-button>
        </div>

        <!-- 新建对话按钮 -->
        <div class="sidebar-action">
          <a-button
            v-if="!isSidebarCollapsed"
            type="primary"
            long
            size="small"
            @click="handleNewChat"
          >
            <template #icon><icon-plus /></template>
            新建对话
          </a-button>
          <a-tooltip v-else content="新建对话" position="right">
            <a-button type="primary" size="small" @click="handleNewChat">
              <template #icon><icon-plus /></template>
            </a-button>
          </a-tooltip>
        </div>

        <!-- 对话列表 -->
        <div class="sidebar-list">
          <template v-if="!isSidebarCollapsed">
            <div
              v-for="session in chatSessions"
              :key="session.id"
              class="session-item"
              :class="{ active: session.id === activeChatSessionId }"
              @click="handleSwitchSession(session.id)"
            >
              <div class="session-title">{{ session.title }}</div>
              <div class="session-preview">{{ session.preview }}</div>
              <div class="session-time">{{ session.lastTime }}</div>
            </div>
          </template>
          <template v-else>
            <a-tooltip
              v-for="session in chatSessions"
              :key="session.id"
              :content="session.title"
              position="right"
            >
              <div
                class="session-dot"
                :class="{ active: session.id === activeChatSessionId }"
                @click="handleSwitchSession(session.id)"
              >
                <icon-message />
              </div>
            </a-tooltip>
          </template>
        </div>
      </div>

      <!-- AI 对话区域 (主工作区) -->
      <div class="chat-panel">
        <div class="chat-content" ref="chatMessagesRef">
        <div class="chat-messages">
          <div
            v-for="(msg, index) in chatMessages"
            :key="index"
            class="chat-message"
            :class="{ 'is-user': msg.isUser }"
          >
            <div class="message-avatar" :class="{ user: msg.isUser }">
              {{ msg.isUser ? '我' : 'AI' }}
            </div>
            <div class="message-body">
              <div class="message-text" v-html="msg.text"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <div class="chat-input-options">
          <a-checkbox v-model="isDeepResearch">深度研究模式</a-checkbox>
        </div>
        <div class="chat-input-wrapper">
          <a-input
            v-model="chatInput"
            placeholder="输入你的问题..."
            allow-clear
            @keypress.enter="handleSendMessage"
          />
          <a-button type="primary" @click="handleSendMessage">
            <template #icon><icon-send /></template>
          </a-button>
        </div>
      </div>
      </div>
    </div>

    <!-- ==================== 数据源抽屉 ==================== -->
    <a-drawer
      v-model:visible="dataSourceDrawerVisible"
      title="选择数据源"
      :width="520"
      :footer="false"
      unmount-on-close
    >
      <a-tabs v-model:active-key="activeSourceTab" type="rounded">
        <!-- 仓库地址 -->
        <a-tab-pane key="repository">
          <template #title>
            <span class="tab-label"><icon-github /> 仓库地址</span>
          </template>
          <div class="source-form">
            <div class="source-form-label">GitHub/GitLab 仓库 URL</div>
            <a-input
              v-model="repositoryUrl"
              placeholder="https://github.com/username/repository"
              allow-clear
            />
            <a-space class="source-form-actions">
              <a-button type="primary" @click="handleParseRepository">
                <template #icon><icon-search /></template>
                解析仓库
              </a-button>
              <a-button @click="repositoryUrl = ''">
                <template #icon><icon-delete /></template>
                清空
              </a-button>
            </a-space>
          </div>
        </a-tab-pane>

        <!-- 网站 -->
        <a-tab-pane key="website">
          <template #title>
            <span class="tab-label"><icon-link /> 网站</span>
          </template>
          <div class="source-form">
            <div class="source-form-label">网站 URL</div>
            <a-input
              v-model="websiteUrl"
              placeholder="https://example.com"
              allow-clear
            />
            <a-space class="source-form-actions">
              <a-button type="primary" @click="handleParseWebsite">
                <template #icon><icon-search /></template>
                解析网站
              </a-button>
              <a-button @click="websiteUrl = ''">
                <template #icon><icon-delete /></template>
                清空
              </a-button>
            </a-space>
          </div>
        </a-tab-pane>

        <!-- 文档上传 -->
        <a-tab-pane key="document">
          <template #title>
            <span class="tab-label"><icon-file /> 文档</span>
          </template>
          <div class="source-form">
            <div class="source-form-label">上传文档</div>
            <a-upload
              draggable
              :limit="1"
              :auto-upload="false"
              tip="支持 PDF, Markdown, TXT 文件 (文件 < 2MB)"
              @change="handleFileChange"
            />
            <a-space class="source-form-actions">
              <a-button type="primary" :disabled="!uploadFileList.length" @click="handleParseDocument">
                <template #icon><icon-search /></template>
                解析文档
              </a-button>
              <a-button @click="handleClearDocument">
                <template #icon><icon-delete /></template>
                清空
              </a-button>
            </a-space>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-drawer>

    <!-- ==================== Wiki 弹窗 (左树右表) ==================== -->
    <a-modal
      v-model:visible="wikiDialogVisible"
      :footer="false"
      width="90%"
      unmount-on-close
      modal-class="wiki-dialog"
    >
      <template #title>
        <div class="dialog-title-bar">
          <span class="dialog-title-text"><icon-book /> 项目 Wiki</span>
        </div>
      </template>
      <div class="dialog-body-layout">
        <a-layout :has-sider="true">
          <!-- 左侧：文档树 -->
          <a-layout-sider
            :width="280"
            :resize-directions="['right']"
            style="min-width: 200px; max-width: 480px"
          >
            <div class="tree-search-bar">
              <a-input
                v-model="wikiSearchKey"
                placeholder="搜索文件名..."
                allow-clear
                size="small"
              >
                <template #prefix><icon-search /></template>
              </a-input>
            </div>
            <a-scrollbar style="height: 680px; overflow: auto">
              <a-tree
                :data="filteredWikiTree"
                :selected-keys="wikiSelectedKeys"
                block-node
                :default-expand-all="true"
                :show-line="true"
                @select="handleWikiNodeSelect"
              >
                <template #title="nodeData">
                  <span class="tree-node-label" :class="{ highlight: isWikiSearchMatch(nodeData.title) }">
                    <icon-folder v-if="nodeData.children?.length" style="margin-right: 6px; color: var(--color-text-3);" />
                    <icon-file v-else style="margin-right: 6px; color: rgb(var(--primary-6));" />
                    {{ nodeData.title }}
                  </span>
                </template>
              </a-tree>
            </a-scrollbar>
          </a-layout-sider>

          <!-- 右侧：文档预览/编辑 -->
          <a-layout-content>
            <a-card :bordered="false">
              <template #title>
                <a-typography-title :heading="6" ellipsis>
                  <icon-file style="margin-right: 4px;" />
                  {{ wikiCurrentFileName || '未选择文件' }}
                </a-typography-title>
              </template>
              <template #extra>
                <a-space>
                  <a-link :disabled="!wikiCurrentFileKey" @click="handleCopyWiki">
                    <template #icon><icon-copy /></template>
                    复制
                  </a-link>
                  <a-link :disabled="!wikiCurrentFileKey" @click="toggleWikiEdit">
                    <template #icon>
                      <icon-edit v-if="!isEditingWiki" />
                      <icon-save v-else />
                    </template>
                    {{ isEditingWiki ? '保存' : '编辑' }}
                  </a-link>
                  <a-link v-if="isEditingWiki" @click="resetWikiEdit">
                    <template #icon><icon-undo /></template>
                    重置
                  </a-link>
                </a-space>
              </template>
              <a-scrollbar style="height: 650px; overflow: auto">
                <!-- 空态 -->
                <div v-if="!wikiCurrentFileKey" class="empty-placeholder">
                  <icon-file size="48" style="opacity: 0.15; margin-bottom: 16px;" />
                  <div>请在左侧选择一个文档</div>
                </div>
                <!-- 预览态 -->
                <div v-else-if="!isEditingWiki" class="markdown-preview" v-html="renderedWikiContent"></div>
                <!-- 编辑态 -->
                <div v-else class="editor-wrapper">
                  <textarea
                    v-model="wikiEditText"
                    class="editor-textarea"
                    spellcheck="false"
                    placeholder="编辑 Markdown 内容..."
                  ></textarea>
                </div>
              </a-scrollbar>
            </a-card>
          </a-layout-content>
        </a-layout>
      </div>
    </a-modal>

    <!-- ==================== 代码预览弹窗 (左树右表) ==================== -->
    <a-modal
      v-model:visible="codePreviewDialogVisible"
      :footer="false"
      :closable="false"
      width="90%"
      unmount-on-close
      modal-class="code-preview-dialog"
    >
      <template #title>
        <div class="dialog-title-bar">
          <span class="dialog-title-text"><icon-code /> 代码预览</span>
          <div class="dialog-title-actions">
            <a-button type="primary" size="small" @click="handleSaveToTemplateLib">
              <template #icon><icon-save /></template>
              保存到模板库
            </a-button>
            <a-divider direction="vertical" />
            <a-button type="secondary" size="small" @click="codePreviewDialogVisible = false">
              <template #icon><icon-close /></template>
              关闭
            </a-button>
          </div>
        </div>
      </template>
      <div class="dialog-body-layout">
        <a-layout :has-sider="true">
          <!-- 左侧：片段列表树 -->
          <a-layout-sider
            :width="280"
            :resize-directions="['right']"
            style="min-width: 200px; max-width: 480px"
          >
            <div class="tree-search-bar">
              <a-input
                v-model="codeSearchKey"
                placeholder="搜索代码片段..."
                allow-clear
                size="small"
              >
                <template #prefix><icon-search /></template>
              </a-input>
            </div>
            <a-scrollbar style="height: 680px; overflow: auto">
              <a-tree
                :data="filteredCodeTree"
                :selected-keys="codeSelectedKeys"
                block-node
                :default-expand-all="true"
                @select="handleCodeNodeSelect"
              >
                <template #title="nodeData">
                  <span class="tree-node-label">
                    <icon-code style="margin-right: 6px; color: rgb(var(--primary-6));" />
                    {{ nodeData.title }}
                  </span>
                </template>
              </a-tree>
            </a-scrollbar>
          </a-layout-sider>

          <!-- 右侧：代码预览/编辑 -->
          <a-layout-content>
            <a-card :bordered="false">
              <template #title>
                <a-typography-title :heading="6" ellipsis>
                  <icon-code style="margin-right: 4px;" />
                  {{ codeCurrentTitle || '未选择代码片段' }}
                </a-typography-title>
              </template>
              <template #extra>
                <a-space>
                  <a-link :disabled="!codeCurrentSnippetId" @click="handleCopyCode">
                    <template #icon><icon-copy /></template>
                    复制
                  </a-link>
                  <a-link :disabled="!codeCurrentSnippetId" @click="toggleCodeEdit">
                    <template #icon>
                      <icon-edit v-if="!isEditingCode" />
                      <icon-save v-else />
                    </template>
                    {{ isEditingCode ? '保存' : '编辑' }}
                  </a-link>
                  <a-link v-if="isEditingCode" @click="resetCodeEdit">
                    <template #icon><icon-undo /></template>
                    重置
                  </a-link>
                </a-space>
              </template>
              <a-scrollbar style="height: 650px; overflow: auto">
                <!-- 空态 -->
                <div v-if="!codeCurrentSnippetId" class="empty-placeholder">
                  <icon-code size="48" style="opacity: 0.15; margin-bottom: 16px;" />
                  <div>请在左侧选择一个代码片段</div>
                </div>
                <!-- 预览态 -->
                <template v-else-if="!isEditingCode">
                  <GiCodeView :type="codeViewType" :code-json="codeCurrentContent" />
                </template>
                <!-- 编辑态 -->
                <div v-else class="editor-wrapper">
                  <textarea
                    v-model="codeEditText"
                    class="editor-textarea code-editor-textarea"
                    spellcheck="false"
                    placeholder="编辑代码..."
                  ></textarea>
                </div>
              </a-scrollbar>
            </a-card>
          </a-layout-content>
        </a-layout>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { Message } from '@arco-design/web-vue'

defineOptions({ name: 'CodeStyleGenerator' })

// ====================== 类型定义 ======================

/** Wiki 文档树节点 */
interface WikiFileNode {
  key: string
  title: string
  content?: string
  children?: WikiFileNode[]
  selectable?: boolean
  isLeaf?: boolean
}

/** 聊天消息 */
interface ChatMessage {
  text: string
  time: string
  isUser: boolean
}

/** 代码片段 */
interface CodeSnippet {
  id: number
  code: string
  language: string
  context: string
  time: string
}

/** 对话会话 */
interface ChatSession {
  id: number
  title: string
  preview: string
  lastTime: string
  messages: ChatMessage[]
}

// ====================== 数据源状态 ======================

/** 数据源抽屉可见性 */
const dataSourceDrawerVisible = ref(false)
/** 当前数据源选项卡 */
const activeSourceTab = ref('repository')
/** 仓库 URL */
const repositoryUrl = ref('')
/** 网站 URL */
const websiteUrl = ref('')
/** 上传的文件列表 */
const uploadFileList = ref<any[]>([])

// ====================== AI 对话状态 ======================

/** 对话输入内容 */
const chatInput = ref('')
/** 是否启用深度研究模式 */
const isDeepResearch = ref(false)
/** 对话内容容器引用 (用于滚动) */
const chatMessagesRef = ref<HTMLElement | null>(null)

// ====================== 对话历史侧边栏状态 ======================

const DEFAULT_AI_GREETING: ChatMessage = {
  text: '你好！我是 AI 助手，已经分析了你的项目。有什么我可以帮助你的吗？',
  time: '刚刚',
  isUser: false,
}

/** 侧边栏是否收起 */
const isSidebarCollapsed = ref(false)
/** 会话自增 ID 计数器 */
let sessionIdCounter = 1
/** 所有对话会话列表 (按时间倒序) */
const chatSessions = ref<ChatSession[]>([
  {
    id: sessionIdCounter,
    title: '新对话',
    preview: DEFAULT_AI_GREETING.text.slice(0, 30),
    lastTime: getCurrentTime(),
    messages: [{ ...DEFAULT_AI_GREETING }],
  },
])
/** 当前激活的会话 ID */
const activeChatSessionId = ref(1)

/** 当前会话的消息列表 (双向绑定到激活会话) */
const chatMessages = computed({
  get(): ChatMessage[] {
    const session = chatSessions.value.find((s) => s.id === activeChatSessionId.value)
    return session?.messages ?? []
  },
  set(messages: ChatMessage[]) {
    const session = chatSessions.value.find((s) => s.id === activeChatSessionId.value)
    if (session) session.messages = messages
  },
})

// ====================== 代码片段状态 ======================

/** 所有代码片段 */
const codeSnippets = ref<CodeSnippet[]>([])
/** 当前选中的片段 ID (列表中) */
const currentSnippetId = ref<number | null>(null)

// ====================== Wiki 弹窗状态 ======================

const wikiDialogVisible = ref(false)
const wikiTreeData = ref<WikiFileNode[]>([])
const wikiSearchKey = ref('')
const wikiSelectedKeys = ref<string[]>([])
const wikiCurrentFileKey = ref('')
const wikiEditText = ref('')
const isEditingWiki = ref(false)

/** Wiki 文档内容缓存 (仅内存，不写入磁盘) */
const wikiDataMap = ref<Record<string, string>>({})

// ====================== 代码预览弹窗状态 ======================

const codePreviewDialogVisible = ref(false)
const codeSearchKey = ref('')
const codeSelectedKeys = ref<string[]>([])
const codeCurrentSnippetId = ref<number | null>(null)
const isEditingCode = ref(false)
const codeEditText = ref('')

// ====================== 计算属性 ======================

/** 当前 Wiki 文件名 */
const wikiCurrentFileName = computed(() => {
  if (!wikiCurrentFileKey.value) return ''
  return findNodeTitle(wikiTreeData.value, wikiCurrentFileKey.value)
})

/** 当前 Wiki 原始 Markdown 内容 */
const wikiCurrentRawContent = computed(() => {
  return wikiDataMap.value[wikiCurrentFileKey.value] || ''
})

/** 渲染后的 Wiki HTML */
const renderedWikiContent = computed(() => {
  return renderMarkdown(wikiCurrentRawContent.value)
})

/** 过滤后的 Wiki 文档树 */
const filteredWikiTree = computed(() => {
  const keyword = wikiSearchKey.value.trim().toLowerCase()
  if (!keyword) return wikiTreeData.value
  return filterTreeByKeyword(wikiTreeData.value, keyword)
})

/** 代码片段树数据 */
const codeTreeData = computed(() => {
  return codeSnippets.value.map((s) => ({
    key: String(s.id),
    title: `${s.language.toUpperCase()} 片段 #${s.id}`,
    isLeaf: true,
  }))
})

/** 过滤后的代码片段树 */
const filteredCodeTree = computed(() => {
  const keyword = codeSearchKey.value.trim().toLowerCase()
  if (!keyword) return codeTreeData.value
  return codeTreeData.value.filter((n) => n.title.toLowerCase().includes(keyword))
})

/** 当前选中代码片段标题 */
const codeCurrentTitle = computed(() => {
  if (!codeCurrentSnippetId.value) return ''
  const snippet = codeSnippets.value.find((s) => s.id === codeCurrentSnippetId.value)
  return snippet ? `${snippet.language.toUpperCase()} 代码片段 #${snippet.id}` : ''
})

/** 当前选中代码片段内容 */
const codeCurrentContent = computed(() => {
  if (!codeCurrentSnippetId.value) return ''
  const snippet = codeSnippets.value.find((s) => s.id === codeCurrentSnippetId.value)
  return snippet?.code || ''
})

/** 代码预览组件的类型标识 */
const codeViewType = computed<'javascript' | 'vue'>(() => {
  if (!codeCurrentSnippetId.value) return 'javascript'
  const snippet = codeSnippets.value.find((s) => s.id === codeCurrentSnippetId.value)
  return snippet?.language === 'vue' ? 'vue' : 'javascript'
})

// ====================== 工具函数 ======================

/** 递归查找树节点标题 */
function findNodeTitle(nodes: WikiFileNode[], key: string): string {
  for (const node of nodes) {
    if (node.key === key) return node.title
    if (node.children) {
      const found = findNodeTitle(node.children, key)
      if (found) return found
    }
  }
  return ''
}

/** 递归过滤树节点 */
function filterTreeByKeyword(nodes: WikiFileNode[], keyword: string): WikiFileNode[] {
  const result: WikiFileNode[] = []
  for (const node of nodes) {
    if (node.title.toLowerCase().includes(keyword)) {
      result.push({ ...node })
    } else if (node.children) {
      const filteredChildren = filterTreeByKeyword(node.children, keyword)
      if (filteredChildren.length > 0) {
        result.push({ ...node, children: filteredChildren })
      }
    }
  }
  return result
}

/** 判断 Wiki 节点是否匹配搜索关键词 */
function isWikiSearchMatch(title: string): boolean {
  const keyword = wikiSearchKey.value.trim().toLowerCase()
  if (!keyword) return false
  return title.toLowerCase().includes(keyword)
}

/** 获取当前时间字符串 (HH:mm) */
function getCurrentTime(): string {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

/** 滚动对话区域到底部 */
function scrollToBottom(): void {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

// ====================== Markdown 渲染器 ======================

/** 简易 Markdown 转 HTML 渲染 */
function renderMarkdown(md: string): string {
  if (!md) return ''
  let html = md

  // 围栏代码块
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, (_m, lang, code) => {
    const escaped = code.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    return `<pre class="md-code-block"><code class="language-${lang}">${escaped}</code></pre>`
  })

  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code class="md-inline-code">$1</code>')

  // 表格
  html = html.replace(
    /^\|(.+)\|\s*\n\|[-| :]+\|\s*\n((?:\|.+\|\s*\n?)*)/gm,
    (_m, header, body) => {
      const ths = header.split('|').map((h: string) => h.trim()).filter(Boolean)
      const headRow = ths.map((h: string) => `<th>${h}</th>`).join('')
      const rows = body.trim().split('\n').map((r: string) => {
        const cells = r.split('|').map((c: string) => c.trim()).filter(Boolean)
        return `<tr>${cells.map((c: string) => `<td>${c}</td>`).join('')}</tr>`
      }).join('')
      return `<table class="md-table"><thead><tr>${headRow}</tr></thead><tbody>${rows}</tbody></table>`
    },
  )

  // 标题 (h1 - h6，带锚点)
  html = html.replace(/^######\s+(.*)$/gm, (_m, t) => `<h6 id="${toAnchorId(t)}">${t}</h6>`)
  html = html.replace(/^#####\s+(.*)$/gm, (_m, t) => `<h5 id="${toAnchorId(t)}">${t}</h5>`)
  html = html.replace(/^####\s+(.*)$/gm, (_m, t) => `<h4 id="${toAnchorId(t)}">${t}</h4>`)
  html = html.replace(/^###\s+(.*)$/gm, (_m, t) => `<h3 id="${toAnchorId(t)}">${t}</h3>`)
  html = html.replace(/^##\s+(.*)$/gm, (_m, t) => `<h2 id="${toAnchorId(t)}">${t}</h2>`)
  html = html.replace(/^#\s+(.*)$/gm, (_m, t) => `<h1 id="${toAnchorId(t)}">${t}</h1>`)

  // 加粗 + 斜体
  html = html.replace(/\*\*\*(.*?)\*\*\*/g, '<strong><em>$1</em></strong>')
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/(?<![*])\*(?!\*)(.+?)(?<![*])\*(?!\*)/g, '<em>$1</em>')

  // 链接
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')

  // 水平线
  html = html.replace(/^---$/gm, '<hr />')

  // 列表项
  html = html.replace(/^[-*]\s+(.*)$/gm, '<li>$1</li>')
  html = html.replace(/^\d+\.\s+(.*)$/gm, '<li>$1</li>')

  // 段落包裹
  const lines = html.split('\n')
  const output: string[] = []
  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) { output.push(''); continue }
    if (/^<[a-zA-Z/]/.test(trimmed)) { output.push(line); continue }
    output.push(`<p>${line}</p>`)
  }
  html = output.join('\n')

  // 将相邻 <li> 包裹为 <ul>
  html = html.replace(/((?:<li>[\s\S]*?<\/li>\s*)+)/g, '<ul>$1</ul>')

  return html
}

/** 将文本转为锚点 ID */
function toAnchorId(text: string): string {
  return text.toLowerCase().replace(/[^\w\u4e00-\u9fff]+/g, '-').replace(/^-|-$/g, '')
}

// ====================== Wiki 方法 ======================

/** 打开 Wiki 弹窗 */
function handleOpenWiki(): void {
  wikiDialogVisible.value = true
}

/** 选择 Wiki 树节点 */
function handleWikiNodeSelect(selectedKeys: (string | number)[], data: any): void {
  if (selectedKeys.length === 0) return
  const key = String(selectedKeys[0])
  // 跳过文件夹节点
  if (data.node?.children?.length) return
  if (isEditingWiki.value) saveWikiEdit()
  wikiSelectedKeys.value = [key]
  wikiCurrentFileKey.value = key
  wikiEditText.value = wikiDataMap.value[key] || ''
  isEditingWiki.value = false
}

/** 复制 Wiki 文档内容 */
function handleCopyWiki(): void {
  const content = wikiCurrentRawContent.value
  if (!content) { Message.warning('没有可复制的内容'); return }
  navigator.clipboard.writeText(content)
    .then(() => Message.success('已复制'))
    .catch(() => Message.error('复制失败'))
}

/** 切换 Wiki 编辑模式 */
function toggleWikiEdit(): void {
  if (isEditingWiki.value) {
    saveWikiEdit()
  } else {
    wikiEditText.value = wikiCurrentRawContent.value
    isEditingWiki.value = true
  }
}

/** 保存 Wiki 编辑内容 */
function saveWikiEdit(): void {
  if (wikiCurrentFileKey.value) {
    wikiDataMap.value[wikiCurrentFileKey.value] = wikiEditText.value
    Message.success('已保存')
  }
  isEditingWiki.value = false
}

/** 重置 Wiki 编辑内容 */
function resetWikiEdit(): void {
  wikiEditText.value = wikiCurrentRawContent.value
  Message.info('已重置')
}

/** 生成示例 Wiki 数据 */
function generateSampleWiki(type: string, source: string): void {
  const files: WikiFileNode[] = [
    {
      key: 'readme',
      title: 'README.md',
      isLeaf: true,
      content: `# 项目文档 — ${type}解析\n\n## 数据源\n\n\`${source}\`\n\n## 项目概述\n\n这是一个自动生成的项目文档，基于对 **${type}** 的分析结果。\n\n## 快速开始\n\n\`\`\`bash\nnpm install\nnpm run dev\n\`\`\`\n\n## 特性\n\n- 自动代码分析\n- 智能文档生成\n- 模板管理\n`,
    },
    {
      key: 'docs',
      title: 'docs',
      selectable: false,
      children: [
        {
          key: 'docs/getting-started',
          title: 'getting-started.md',
          isLeaf: true,
          content: `# 快速入门\n\n## 环境要求\n\n- Node.js >= 16\n- npm >= 8\n\n## 安装\n\n\`\`\`bash\ngit clone ${source}\ncd project\nnpm install\n\`\`\`\n\n## 启动开发服务器\n\n\`\`\`bash\nnpm run dev\n\`\`\`\n\n服务器将在 **http://localhost:3000** 启动。\n`,
        },
        {
          key: 'docs/configuration',
          title: 'configuration.md',
          isLeaf: true,
          content: `# 配置指南\n\n## 环境变量\n\n| 变量名 | 说明 | 默认值 |\n|--------|------|--------|\n| PORT | 服务端口 | 3000 |\n| DB_HOST | 数据库地址 | localhost |\n| DB_PORT | 数据库端口 | 5432 |\n\n## 配置文件\n\n项目使用 \`.env\` 文件管理环境变量。\n\n\`\`\`\nPORT=3000\nDB_HOST=localhost\nDB_PORT=5432\n\`\`\`\n`,
        },
        {
          key: 'docs/architecture',
          title: 'architecture.md',
          isLeaf: true,
          content: `# 架构设计\n\n## 技术栈\n\n- **前端**: Vue 3 + TypeScript + Arco Design\n- **构建工具**: Vite\n- **状态管理**: Pinia\n- **路由**: Vue Router\n\n## 目录结构\n\n\`\`\`\nsrc/\n├── apis/        # API 接口\n├── components/  # 公共组件\n├── hooks/       # 组合式函数\n├── router/      # 路由配置\n├── stores/      # 状态管理\n├── utils/       # 工具函数\n└── views/       # 页面视图\n\`\`\`\n`,
        },
      ],
    },
    {
      key: 'api',
      title: 'api',
      selectable: false,
      children: [
        {
          key: 'api/overview',
          title: 'overview.md',
          isLeaf: true,
          content: `# API 概览\n\n## 接口规范\n\n所有接口均使用 RESTful 风格。\n\n### 请求格式\n\n- Content-Type: \`application/json\`\n- 认证方式: Bearer Token\n\n### 响应格式\n\n\`\`\`json\n{\n  "code": 200,\n  "message": "success",\n  "data": {}\n}\n\`\`\`\n`,
        },
        {
          key: 'api/endpoints',
          title: 'endpoints.md',
          isLeaf: true,
          content: `# API 端点\n\n## 用户模块\n\n### 获取用户列表\n\n\`\`\`\nGET /api/users\n\`\`\`\n\n**参数**\n\n| 参数 | 类型 | 必填 | 说明 |\n|------|------|------|------|\n| page | number | 否 | 页码 |\n| size | number | 否 | 每页数量 |\n\n### 创建用户\n\n\`\`\`\nPOST /api/users\n\`\`\`\n\n**请求体**\n\n\`\`\`json\n{\n  "username": "string",\n  "email": "string",\n  "role": "string"\n}\n\`\`\`\n`,
        },
      ],
    },
    {
      key: 'changelog',
      title: 'CHANGELOG.md',
      isLeaf: true,
      content: `# 更新日志\n\n## v1.0.0 (2026-02-13)\n\n### 新增\n\n- 初始版本发布\n- 仓库解析功能\n- Wiki 文档生成\n- AI 对话助手\n\n### 修复\n\n- 修复了若干已知问题\n\n## v0.9.0 (2026-01-15)\n\n### 新增\n\n- Beta 测试版本\n- 基础代码分析功能\n`,
    },
  ]

  wikiTreeData.value = files

  // 构建内存 Wiki 数据映射
  wikiDataMap.value = {}
  buildWikiDataMap(files)

  // 默认选中第一个文件
  wikiSelectedKeys.value = ['readme']
  wikiCurrentFileKey.value = 'readme'
  wikiEditText.value = wikiDataMap.value.readme || ''
  isEditingWiki.value = false
}

/** 递归构建 Wiki 数据映射表 */
function buildWikiDataMap(nodes: WikiFileNode[]): void {
  for (const node of nodes) {
    if (node.content) wikiDataMap.value[node.key] = node.content
    if (node.children) buildWikiDataMap(node.children)
  }
}

// ====================== 代码预览弹窗方法 ======================

/** 打开代码预览弹窗 */
function handleOpenCodePreview(): void {
  if (codeSnippets.value.length === 0) return
  codePreviewDialogVisible.value = true
  // 默认选中第一个片段
  if (!codeCurrentSnippetId.value && codeSnippets.value.length > 0) {
    const first = codeSnippets.value[0]
    codeCurrentSnippetId.value = first.id
    codeSelectedKeys.value = [String(first.id)]
  }
}

/** 选择代码片段树节点 */
function handleCodeNodeSelect(selectedKeys: (string | number)[]): void {
  if (selectedKeys.length === 0) return
  if (isEditingCode.value) saveCodeEdit()
  const id = Number(selectedKeys[0])
  codeCurrentSnippetId.value = id
  codeSelectedKeys.value = [String(id)]
  isEditingCode.value = false
}

/** 复制代码片段内容 */
function handleCopyCode(): void {
  const content = codeCurrentContent.value
  if (!content) { Message.warning('没有可复制的内容'); return }
  navigator.clipboard.writeText(content)
    .then(() => Message.success('已复制'))
    .catch(() => Message.error('复制失败'))
}

/** 切换代码片段编辑模式 */
function toggleCodeEdit(): void {
  if (isEditingCode.value) {
    saveCodeEdit()
  } else {
    codeEditText.value = codeCurrentContent.value
    isEditingCode.value = true
  }
}

/** 保存代码片段编辑 */
function saveCodeEdit(): void {
  if (codeCurrentSnippetId.value) {
    const snippet = codeSnippets.value.find((s) => s.id === codeCurrentSnippetId.value)
    if (snippet) {
      snippet.code = codeEditText.value
      Message.success('已保存')
    }
  }
  isEditingCode.value = false
}

/** 重置代码片段编辑 */
function resetCodeEdit(): void {
  codeEditText.value = codeCurrentContent.value
  Message.info('已重置')
}

/** 保存到模板库 */
function handleSaveToTemplateLib(): void {
  Message.success('已保存到模板库')
  // TODO: 对接实际模板库 API
}

// ====================== 数据源解析方法 ======================

/** 解析仓库 */
function handleParseRepository(): void {
  if (!repositoryUrl.value) { Message.warning('请输入仓库 URL'); return }
  generateSampleWiki('仓库', repositoryUrl.value)
  dataSourceDrawerVisible.value = false
  wikiDialogVisible.value = true
}

/** 解析网站 */
function handleParseWebsite(): void {
  if (!websiteUrl.value) { Message.warning('请输入网站 URL'); return }
  generateSampleWiki('网站', websiteUrl.value)
  dataSourceDrawerVisible.value = false
  wikiDialogVisible.value = true
}

/** 文件变更回调 */
function handleFileChange(fileListArg: any[]): void {
  uploadFileList.value = fileListArg
}

/** 解析文档 */
function handleParseDocument(): void {
  if (!uploadFileList.value.length) { Message.warning('请先上传文档'); return }
  generateSampleWiki('文档', uploadFileList.value[0].name)
  dataSourceDrawerVisible.value = false
  wikiDialogVisible.value = true
}

/** 清空上传文件 */
function handleClearDocument(): void {
  uploadFileList.value = []
}

// ====================== AI 对话方法 ======================

/** 清空当前对话记录 */
function handleClearChat(): void {
  chatMessages.value = [{ ...DEFAULT_AI_GREETING, time: getCurrentTime() }]
  syncSessionMeta()
}

// ====================== 侧边栏方法 ======================

/** 切换侧边栏展开/收起 */
function toggleSidebar(): void {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

/** 新建对话 */
function handleNewChat(): void {
  sessionIdCounter += 1
  const newSession: ChatSession = {
    id: sessionIdCounter,
    title: '新对话',
    preview: DEFAULT_AI_GREETING.text.slice(0, 30),
    lastTime: getCurrentTime(),
    messages: [{ ...DEFAULT_AI_GREETING, time: getCurrentTime() }],
  }
  chatSessions.value.unshift(newSession)
  activeChatSessionId.value = newSession.id
  chatInput.value = ''
  scrollToBottom()
}

/** 切换到指定会话 */
function handleSwitchSession(sessionId: number): void {
  if (sessionId === activeChatSessionId.value) return
  activeChatSessionId.value = sessionId
  chatInput.value = ''
  scrollToBottom()
}

/** 同步当前会话的标题/预览/时间 (在有新消息时调用) */
function syncSessionMeta(): void {
  const session = chatSessions.value.find((s) => s.id === activeChatSessionId.value)
  if (!session || session.messages.length === 0) return
  const lastMsg = session.messages[session.messages.length - 1]
  // 提取第一条用户消息作为标题，如果没有用户消息则保留默认
  const firstUserMsg = session.messages.find((m) => m.isUser)
  if (firstUserMsg) {
    // 去除 HTML 标签后截取
    const plainText = firstUserMsg.text.replace(/<[^>]*>/g, '')
    session.title = plainText.length > 20 ? `${plainText.slice(0, 20)}...` : plainText
  }
  const previewPlain = lastMsg.text.replace(/<[^>]*>/g, '')
  session.preview = previewPlain.length > 30 ? `${previewPlain.slice(0, 30)}...` : previewPlain
  session.lastTime = lastMsg.time
}

/** 发送消息 */
function handleSendMessage(): void {
  if (!chatInput.value.trim()) return

  const message = chatInput.value.trim()
  const time = getCurrentTime()

  // 添加用户消息
  chatMessages.value.push({
    text: message + (isDeepResearch.value ? ' <span class="badge">深度研究</span>' : ''),
    time,
    isUser: true,
  })
  chatInput.value = ''
  syncSessionMeta()
  scrollToBottom()

  // 模拟 AI 响应
  setTimeout(() => {
    let aiResponse = ''
    let codeSnippetStr = ''

    if (isDeepResearch.value) {
      aiResponse = `[深度研究模式] 正在对 "${message}" 进行深入分析...<br><br>`
        + '📋 分析步骤：<br>1. 分解为子任务<br>2. 分析相关代码模块<br>3. 提取关键代码片段<br>4. 生成综合报告<br><br>'
    }

    if (message.toLowerCase().includes('vue') || message.toLowerCase().includes('组件')) {
      aiResponse += '好的，我为你生成了一个 Vue 组件示例。'
      codeSnippetStr = [
        '<template>',
        '  <div class="user-card">',
        '    <h3>{{ user.name }}</h3>',
        '    <p>{{ user.email }}</p>',
        '    <button @click="handleClick">操作</button>',
        '  </div>',
        '</template>',
        '',
        '<script setup>',
        'import { ref } from \'vue\';',
        '',
        'const user = ref({',
        '  name: \'张三\',',
        '  email: \'zhangsan@example.com\',',
        '});',
        '',
        'const handleClick = () => {',
        '  console.log(\'Clicked\');',
        '};',
        '<\/script>',
        '',
        '<style scoped>',
        '.user-card {',
        '  padding: 1rem;',
        '  border: 1px solid var(--color-border);',
        '  border-radius: 2px;',
        '}',
        '</style>',
      ].join('\n')
    } else {
      aiResponse += '我理解你的问题。让我帮你分析一下... 这里是一个示例代码：'
      codeSnippetStr = 'function greet(name) {\n  return `你好, ${name}!`;\n}\n\nconsole.log(greet(\'世界\'));'
    }

    if (isDeepResearch.value) {
      aiResponse += '<br><br>✅ 深度研究完成！以上是详细的分析结果。'
    }

    chatMessages.value.push({ text: aiResponse, time: getCurrentTime(), isUser: false })
    syncSessionMeta()

    if (codeSnippetStr) addCodeSnippet(codeSnippetStr, message)
    scrollToBottom()
  }, 800)
}

/** 添加代码片段 */
function addCodeSnippet(code: string, context: string): void {
  const id = codeSnippets.value.length + 1
  const language = code.includes('<template>') ? 'vue' : 'javascript'

  codeSnippets.value.push({
    id,
    code,
    language,
    context,
    time: getCurrentTime(),
  })

  if (codeSnippets.value.length === 1) {
    currentSnippetId.value = id
  }
}
</script>

<style scoped lang="scss">
/* ===================== 页面布局 ===================== */
.generate-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0;
  overflow: hidden;
}

.main-content {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* ===================== 对话历史侧边栏 ===================== */
$sidebar-width-expanded: 260px;
$sidebar-width-collapsed: 52px;

.history-sidebar {
  width: $sidebar-width-expanded;
  min-width: $sidebar-width-expanded;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-2);
  border-right: 1px solid var(--color-border);
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1),
              min-width 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &.collapsed {
    width: $sidebar-width-collapsed;
    min-width: $sidebar-width-collapsed;
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--color-border);
  min-height: 44px;

  .collapsed & {
    justify-content: center;
    padding: 12px 8px;
  }
}

.sidebar-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-1);
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-toggle-btn {
  flex-shrink: 0;
}

.sidebar-action {
  padding: 8px 12px;
  flex-shrink: 0;

  .collapsed & {
    padding: 8px;
    display: flex;
    justify-content: center;
  }
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 8px;
}

.session-item {
  padding: 10px 12px;
  border-radius: 2px;
  cursor: pointer;
  margin-bottom: 2px;
  transition: background 0.2s;
  overflow: hidden;

  &:hover {
    background: var(--color-fill-2);
  }

  &.active {
    background: rgba(var(--primary-6), 0.08);
  }
}

.session-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;

  .session-item.active & {
    color: rgb(var(--primary-6));
  }
}

.session-preview {
  font-size: 12px;
  color: var(--color-text-3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.session-time {
  font-size: 11px;
  color: var(--color-text-4);
}

.session-dot {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px;
  cursor: pointer;
  margin: 0 auto 4px;
  color: var(--color-text-3);
  transition: background 0.2s, color 0.2s;

  &:hover {
    background: var(--color-fill-2);
    color: var(--color-text-1);
  }

  &.active {
    background: rgba(var(--primary-6), 0.08);
    color: rgb(var(--primary-6));
  }
}

/* ===================== 顶部工具栏 ===================== */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-2);
  flex-shrink: 0;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
}

/* ===================== AI 对话面板 ===================== */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--color-bg-1);
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-message {
  display: flex;
  gap: 12px;
}

.message-avatar {
  width: 32px;
  height: 32px;
  background: var(--color-fill-3);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-2);

  &.user {
    background: rgb(var(--primary-6));
    color: #fff;
  }
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-text {
  background: var(--color-fill-2);
  padding: 12px;
  border-radius: 2px;
  color: var(--color-text-1);
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: var(--color-text-4);
  margin-top: 4px;
}

/* ===================== 聊天输入区域 ===================== */
.chat-input-area {
  padding: 12px 16px;
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-2);
  flex-shrink: 0;
}

.chat-input-options {
  margin-bottom: 8px;
}

.chat-input-wrapper {
  display: flex;
  gap: 8px;
}

/* ===================== 数据源抽屉 ===================== */
.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.source-form {
  padding: 16px 0;
}

.source-form-label {
  margin-bottom: 8px;
  color: var(--color-text-2);
  font-weight: 500;
  font-size: 14px;
}

.source-form-actions {
  margin-top: 16px;
}

/* ===================== 弹窗通用样式 ===================== */
.dialog-title-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.dialog-title-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.dialog-title-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-body-layout {
  background: var(--color-bg-2);
}

/* ===================== 树形搜索/节点 ===================== */
.tree-search-bar {
  padding: 12px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-1);
}

.tree-node-label {
  display: inline-flex;
  align-items: center;
  font-size: 13px;

  &.highlight {
    color: rgb(var(--primary-6));
    font-weight: 600;
  }
}

/* ===================== 空态提示 ===================== */
.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 60px 20px;
  color: var(--color-text-3);
  font-size: 14px;
}

/* ===================== Markdown 预览 ===================== */
.markdown-preview {
  padding: 24px 32px;
  color: var(--color-text-1);
  line-height: 1.8;
  font-size: 14px;

  :deep(h1) {
    font-size: 2em;
    margin: 0.67em 0;
    padding-bottom: 0.3em;
    border-bottom: 1px solid var(--color-border);
  }

  :deep(h2) {
    font-size: 1.5em;
    margin: 1em 0 0.5em;
    padding-bottom: 0.3em;
    border-bottom: 1px solid var(--color-border);
  }

  :deep(h3) {
    font-size: 1.25em;
    margin: 1em 0 0.5em;
  }

  :deep(h4),
  :deep(h5),
  :deep(h6) {
    margin: 1em 0 0.5em;
  }

  :deep(p) {
    margin: 0.5em 0;
  }

  :deep(a) {
    color: rgb(var(--primary-6));
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(ul) {
    padding-left: 2em;
    margin: 0.5em 0;
  }

  :deep(li) {
    margin: 0.25em 0;
    list-style: disc;
  }

  :deep(hr) {
    border: none;
    border-top: 1px solid var(--color-border);
    margin: 1.5em 0;
  }

  :deep(strong) {
    font-weight: 600;
  }

  :deep(.md-code-block) {
    background: var(--color-fill-2);
    border: 1px solid var(--color-border);
    border-radius: 2px;
    padding: 16px;
    overflow-x: auto;
    margin: 1em 0;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 13px;
    line-height: 1.5;

    code {
      background: none;
      padding: 0;
      border: none;
      font-size: inherit;
    }
  }

  :deep(.md-inline-code) {
    background: var(--color-fill-2);
    padding: 2px 6px;
    border-radius: 2px;
    font-family: 'SFMono-Regular', Consolas, monospace;
    font-size: 0.9em;
    border: 1px solid var(--color-border);
  }

  :deep(.md-table) {
    width: 100%;
    border-collapse: collapse;
    margin: 1em 0;

    th,
    td {
      border: 1px solid var(--color-border);
      padding: 8px 12px;
      text-align: left;
    }

    th {
      background: var(--color-fill-2);
      font-weight: 600;
    }

    tr:nth-child(even) td {
      background: var(--color-fill-1);
    }
  }
}

/* ===================== 编辑器 ===================== */
.editor-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-textarea {
  width: 100%;
  height: 100%;
  border: none;
  outline: none;
  resize: none;
  padding: 24px 32px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 14px;
  line-height: 1.6;
  background: var(--color-bg-1);
  color: var(--color-text-1);
  box-sizing: border-box;
}

.code-editor-textarea {
  tab-size: 2;
}
</style>

<!-- 弹窗全局样式 (非 scoped) -->
<style lang="scss">
.wiki-dialog,
.code-preview-dialog {
  .arco-modal-header {
    padding: 16px 24px;
    border-bottom: 1px solid var(--color-border);
    background: var(--color-bg-1);
  }

  .arco-modal-title {
    font-weight: 600;
    font-size: 16px;
  }

  .arco-modal-body {
    padding: 0 !important;
    overflow: hidden;
    background: var(--color-bg-2);
  }

  .arco-link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: 2px;
    font-size: 13px;
    color: var(--color-text-2);
    transition: color 0.2s, background 0.2s;

    &:hover {
      color: rgb(var(--primary-6));
      background: rgba(var(--primary-6), 0.06);
    }

    &.arco-link-disabled {
      color: var(--color-text-4);
      cursor: not-allowed;
    }
  }
}
</style>
