export interface TagItem {
  label: string
  color: string
}

export interface TemplateQuery {
  keyword?: string
}

export interface TemplatePageQuery extends TemplateQuery, PageQuery {}

export interface TemplateReq {
  groupId?: string
  artifactId?: string
  name: string
  icon?: string
  author?: string
  description?: string
  version?: string
  downloadUrl?: string
  searchRefId?: string
  tags?: TagItem[]
}

export interface TemplateItem {
  id: number
  groupId?: string
  artifactId?: string
  name: string
  icon: string
  author: string
  description: string
  tags: TagItem[]
  downloadCount: number
  rating: number
  isFavorite: boolean
  version?: string
  createTime: string
  updateTime: string
}

export interface TemplateDetail extends TemplateItem {
  createUserString?: string
  starCount?: number
  downloadUrl?: string
  searchRefId?: string
}

export interface ChatSession {
  id: number
  title: string
  preview: string
  lastTime: string
  createTime: string
}

export interface ChatMessage {
  id?: number
  sessionId?: number
  role: string
  content: string
  createTime?: string
}

export interface CodeSnippet {
  id: number
  sessionId?: number
  messageId?: number
  code: string
  language: string
  context: string
  createTime: string
}

export interface ChatRequest {
  sessionId: number
  message: string
  deepResearch?: boolean
}

export interface ChatResponse {
  reply: ChatMessage
  codeSnippets: CodeSnippet[]
}

export interface SaveToLibraryReq {
  name?: string
  description?: string
  tags?: TagItem[]
}

export interface ResearchStartReq {
  sourceType?: string
  sourceContent: string
  templateName?: string
  templateDescription?: string
  autoConfirm?: boolean
}

export interface ResearchStatus {
  taskId: string
  status: string
  currentNode?: string
  error?: string
}

export interface TemplateUploadResp {
  templateId: string
  groupId: string
  artifactId: string
  version: string
  name: string
  description: string
  downloadUrl: string
  files: TemplateFileInfo[]
  uploadTime: string
}

export interface TemplateFileInfo {
  filename: string
  filePath: string
  description: string
  fileUrl: string
}

export interface FileTreeNode {
  name: string
  path: string
  isDir: boolean
  size?: number
  children?: FileTreeNode[]
}
