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
          <a-button :disabled="codeSnippets.length === 0" @click="handleOpenCodePreview">
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
        <div ref="chatMessagesRef" class="chat-content">
          <div class="chat-messages">
            <div
              v-for="(msg, index) in chatMessages"
              :key="index"
              class="chat-message"
              :class="{ 'is-user': msg.role === 'user' }"
            >
              <div class="message-avatar" :class="{ user: msg.role === 'user' }">
                {{ msg.role === 'user' ? '我' : 'AI' }}
              </div>
              <div class="message-body">
                <div class="message-text" v-html="msg.content"></div>
                <div class="message-time">{{ msg.createTime || '' }}</div>
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
import { computed, nextTick, onMounted, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import type { ChatMessage, ChatSession, CodeSnippet, ResearchStartReq } from '@/apis/template/type'
import {
  createSession as apiCreateSession,
  updateSnippet as apiUpdateSnippet,
  getSessionMessages,
  getSessionSnippets,
  listSessions,
  saveSnippetToLibrary,
  sendChatMessage,
  startResearch,
  subscribeResearchProgress,
} from '@/apis/template/generate'

defineOptions({ name: 'CodeStyleGenerator' })

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
  role: 'assistant',
  content: '你好！我是 AI 助手，有什么我可以帮助你的吗？',
}

/** 侧边栏是否收起 */
const isSidebarCollapsed = ref(false)
/** 所有对话会话列表 (按时间倒序) */
const chatSessions = ref<ChatSession[]>([])
/** 当前激活的会话 ID */
const activeChatSessionId = ref<number | null>(null)

/** 当前会话的消息列表 */
const chatMessages = ref<ChatMessage[]>([{ ...DEFAULT_AI_GREETING }])

// ====================== 代码片段状态 ======================

/** 所有代码片段 */
const codeSnippets = ref<CodeSnippet[]>([])

// ====================== 研究任务状态 ======================

const researchTaskId = ref<string | null>(null)
const researchStatus = ref<string>('')
const researchEventSource = ref<EventSource | null>(null)

// ====================== 代码预览弹窗状态 ======================

const codePreviewDialogVisible = ref(false)
const codeSearchKey = ref('')
const codeSelectedKeys = ref<string[]>([])
const codeCurrentSnippetId = ref<number | null>(null)
const isEditingCode = ref(false)
const codeEditText = ref('')

// ====================== 计算属性 ======================

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

/** 滚动对话区域到底部 */
function scrollToBottom(): void {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

// ====================== Wiki 方法 (已移至 research 模块) ======================

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
  if (!content) {
    Message.warning('没有可复制的内容')
    return
  }
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
async function saveCodeEdit(): Promise<void> {
  if (codeCurrentSnippetId.value) {
    const snippet = codeSnippets.value.find((s) => s.id === codeCurrentSnippetId.value)
    if (snippet) {
      try {
        await apiUpdateSnippet(snippet.id, codeEditText.value)
        snippet.code = codeEditText.value
        Message.success('已保存')
      } catch {
        Message.error('保存失败')
      }
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
async function handleSaveToTemplateLib(): Promise<void> {
  if (!codeCurrentSnippetId.value) return
  try {
    await saveSnippetToLibrary(codeCurrentSnippetId.value)
    Message.success('已保存到模板库')
  } catch {
    Message.error('保存失败')
  }
}

// ====================== 数据源研究方法 ======================

async function handleStartResearch(sourceType: string, sourceContent: string): Promise<void> {
  if (!sourceContent) {
    Message.warning('请输入内容')
    return
  }
  try {
    const req: ResearchStartReq = { sourceType, sourceContent }
    const res = await startResearch(req)
    researchTaskId.value = res.data.taskId
    researchStatus.value = res.data.status
    dataSourceDrawerVisible.value = false
    Message.success('研究任务已启动')

    const es = subscribeResearchProgress(res.data.taskId)
    researchEventSource.value = es
    es.onmessage = (event) => {
      const data = JSON.parse(event.data)
      researchStatus.value = data.status
      if (data.status === 'COMPLETED' || data.status === 'FAILED') {
        es.close()
        researchEventSource.value = null
        if (data.status === 'COMPLETED') Message.success('研究任务完成')
        else Message.error(data.error || '研究任务失败')
      }
    }
    es.onerror = () => {
      es.close()
      researchEventSource.value = null
    }
  } catch {
    Message.error('启动研究任务失败')
  }
}

function handleParseRepository(): void {
  handleStartResearch('GITHUB', repositoryUrl.value)
}

function handleParseWebsite(): void {
  handleStartResearch('URL', websiteUrl.value)
}

function handleFileChange(fileListArg: any[]): void {
  uploadFileList.value = fileListArg
}

async function handleParseDocument(): Promise<void> {
  if (!uploadFileList.value.length) {
    Message.warning('请先上传文档')
    return
  }
  const file = uploadFileList.value[0].file as File
  handleStartResearch('FILE', file.name)
}

function handleClearDocument(): void {
  uploadFileList.value = []
}

// ====================== AI 对话方法 ======================

/** 清空当前对话记录 */
function handleClearChat(): void {
  chatMessages.value = [{ ...DEFAULT_AI_GREETING }]
}

// ====================== 侧边栏方法 ======================

/** 切换侧边栏展开/收起 */
function toggleSidebar(): void {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

/** 新建对话 */
async function handleNewChat(): Promise<void> {
  try {
    const res = await apiCreateSession()
    chatSessions.value.unshift(res.data)
    activeChatSessionId.value = res.data.id
    chatMessages.value = [{ ...DEFAULT_AI_GREETING }]
    codeSnippets.value = []
    chatInput.value = ''
    scrollToBottom()
  } catch {
    Message.error('创建会话失败')
  }
}

/** 切换到指定会话 */
async function handleSwitchSession(sessionId: number): Promise<void> {
  if (sessionId === activeChatSessionId.value) return
  activeChatSessionId.value = sessionId
  chatInput.value = ''
  try {
    const [msgRes, snippetRes] = await Promise.all([
      getSessionMessages(sessionId),
      getSessionSnippets(sessionId),
    ])
    chatMessages.value = msgRes.data.length > 0 ? msgRes.data : [{ ...DEFAULT_AI_GREETING }]
    codeSnippets.value = snippetRes.data
  } catch {
    chatMessages.value = [{ ...DEFAULT_AI_GREETING }]
    codeSnippets.value = []
  }
  scrollToBottom()
}

/** 同步当前会话的标题/预览/时间 (在有新消息时调用) */
function syncSessionMeta(lastMsg: ChatMessage): void {
  const session = chatSessions.value.find((s) => s.id === activeChatSessionId.value)
  if (!session) return
  const firstUserMsg = chatMessages.value.find((m) => m.role === 'user')
  if (firstUserMsg) {
    const plainText = firstUserMsg.content.replace(/<[^>]*>/g, '')
    session.title = plainText.length > 20 ? `${plainText.slice(0, 20)}...` : plainText
  }
  const previewPlain = lastMsg.content.replace(/<[^>]*>/g, '')
  session.preview = previewPlain.length > 30 ? `${previewPlain.slice(0, 30)}...` : previewPlain
  session.lastTime = lastMsg.createTime || ''
}

/** 发送消息 */
async function handleSendMessage(): Promise<void> {
  if (!chatInput.value.trim()) return
  if (!activeChatSessionId.value) {
    // 自动创建新会话
    try {
      const res = await apiCreateSession()
      chatSessions.value.unshift(res.data)
      activeChatSessionId.value = res.data.id
    } catch {
      Message.error('创建会话失败')
      return
    }
  }

  const message = chatInput.value.trim()

  const userMsg: ChatMessage = {
    content: message + (isDeepResearch.value ? ' <span class="badge">深度研究</span>' : ''),
    role: 'user',
  }
  chatMessages.value.push(userMsg)
  chatInput.value = ''
  syncSessionMeta(userMsg)
  scrollToBottom()

  try {
    const res = await sendChatMessage({
      sessionId: activeChatSessionId.value!,
      message,
      deepResearch: isDeepResearch.value,
    })

    const aiMsg: ChatMessage = {
      content: res.data.reply.content,
      role: 'assistant',
      createTime: res.data.reply.createTime,
    }
    chatMessages.value.push(aiMsg)
    syncSessionMeta(aiMsg)

    if (res.data.codeSnippets?.length) {
      codeSnippets.value.push(...res.data.codeSnippets)
    }
  } catch {
    chatMessages.value.push({
      content: '抱歉，请求出错了，请稍后重试。',
      role: 'assistant',
    })
  }
  scrollToBottom()
}

/** 初始化：加载会话列表 */
onMounted(async () => {
  try {
    const res = await listSessions()
    chatSessions.value = res.data
    if (chatSessions.value.length > 0) {
      await handleSwitchSession(chatSessions.value[0].id)
    }
  } catch {
    // 首次使用无会话，忽略
  }
})
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
