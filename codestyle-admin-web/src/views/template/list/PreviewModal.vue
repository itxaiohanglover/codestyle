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
        <a-layout-sider :width="340" :resize-directions="['right']" style="min-width: 260px; max-width: 500px">
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
                <div class="section-text">{{ templateData?.description }}</div>
              </div>

              <!-- 统计数据 -->
              <div class="stats-grid">
                <div class="stat-card">
                  <icon-star-fill class="stat-icon" style="color: rgb(var(--warning-6))" />
                  <div class="stat-value">2,543</div>
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
                <span class="version-label">当前版本</span>
                <span class="version-number">v3.2.0</span>
              </div>

              <!-- 元数据 -->
              <div class="meta-section">
                <div class="meta-item">
                  <div class="meta-icon"><icon-user /></div>
                  <div class="meta-content">
                    <div class="meta-label">创建者</div>
                    <div class="meta-value">{{ templateData?.author }}</div>
                  </div>
                </div>
                <div class="meta-item">
                  <div class="meta-icon"><icon-clock-circle /></div>
                  <div class="meta-content">
                    <div class="meta-label">创建时间</div>
                    <div class="meta-value">2024-01-15</div>
                  </div>
                </div>
                <div class="meta-item">
                  <div class="meta-icon"><icon-calendar /></div>
                  <div class="meta-content">
                    <div class="meta-label">更新时间</div>
                    <div class="meta-value">2026-02-01</div>
                  </div>
                </div>
              </div>

              <!-- 下载按钮 -->
              <a-button type="primary" long @click="onDownload">
                <template #icon><icon-download /></template>
                下载压缩包
              </a-button>
            </div>
          </a-scrollbar>
        </a-layout-sider>

        <!-- 文件树侧边栏 -->
        <a-layout-sider :width="280" :resize-directions="['right']" style="min-width: 200px; max-width: 400px">
          <div class="file-tree-header">
            <icon-folder />
            <span>文件目录</span>
          </div>
          <a-scrollbar style="height: 660px; overflow: auto">
            <a-tree
              ref="treeRef"
              :data="treeData"
              show-line
              block-node
              :selected-keys="selectedKeys"
              @select="onSelectFile"
            >
              <template #switcher-icon="node, { isLeaf }">
                <icon-caret-down v-if="!isLeaf" />
              </template>
              <template #icon="node">
                <GiSvgIcon v-if="!node.isLeaf && !node.expanded" :size="16" name="directory-blue" />
                <GiSvgIcon v-if="!node.isLeaf && node.expanded" :size="16" name="directory-open-blue" />
                <GiSvgIcon v-if="node.isLeaf && checkFileType(node.node.title, '.java')" :size="16" name="file-java" />
                <GiSvgIcon v-if="node.isLeaf && checkFileType(node.node.title, '.vue')" :size="16" name="file-vue" />
                <GiSvgIcon v-if="node.isLeaf && checkFileType(node.node.title, '.ts')" :size="16" name="file-typescript" />
                <GiSvgIcon v-if="node.isLeaf && checkFileType(node.node.title, '.yml')" :size="16" name="file-json" />
                <GiSvgIcon v-if="node.isLeaf && checkFileType(node.node.title, '.xml')" :size="16" name="file-xml" />
                <GiSvgIcon v-if="node.isLeaf && checkFileType(node.node.title, '.md')" :size="16" name="file-txt" />
              </template>
            </a-tree>
          </a-scrollbar>
        </a-layout-sider>

        <!-- 代码预览区 -->
        <a-layout-content>
          <a-card :bordered="false">
            <template #title>
              <a-typography-title :heading="6" ellipsis>{{ currentFile?.title }}</a-typography-title>
            </template>
            <template #extra>
              <a-link @click="onCopy">
                <template #icon><icon-copy /></template>
                复制
              </a-link>
            </template>
            <a-scrollbar style="height: 650px; overflow: auto">
              <GiCodeView :type="codeType" :code-json="currentFile?.content || ''" />
            </a-scrollbar>
          </a-card>
        </a-layout-content>
      </a-layout>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { Message, type TreeNodeData } from '@arco-design/web-vue'
import { useClipboard } from '@vueuse/core'

const { copy, copied } = useClipboard()

interface TagItem {
  label: string
  color: string
}

interface TemplateItem {
  id: number
  name: string
  icon: string
  author: string
  description: string
  tags: TagItem[]
  downloadCount: string
  rating: number
  isFavorite: boolean
}

interface FileNode {
  title: string
  key: string
  content?: string
  children?: FileNode[]
  isLeaf?: boolean
}

const visible = ref(false)
const templateData = ref<TemplateItem>()
const iconStyle = ref<Record<string, string>>({})

// 图标渐变色
const iconGradients = [
  'linear-gradient(135deg, #0891B2 0%, #22D3EE 100%)',
  'linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)',
  'linear-gradient(135deg, #059669 0%, #34D399 100%)',
  'linear-gradient(135deg, #DC2626 0%, #F87171 100%)',
  'linear-gradient(135deg, #D97706 0%, #FBBF24 100%)',
  'linear-gradient(135deg, #2563EB 0%, #60A5FA 100%)',
]

// Mock 文件树数据
const mockTreeData: FileNode[] = [
  {
    title: 'README.md',
    key: 'README.md',
    isLeaf: true,
    content: '# Spring Boot CRUD 模板\n\n## 项目简介\n\n这是一个标准的 Spring Boot 增删改查模板，包含完整的 Controller、Service、Repository 层代码结构。\n\n## 功能特性\n\n- 完整的 CRUD 操作\n- 分页查询支持\n- 高级查询条件\n- 参数校验\n- 统一异常处理\n- RESTful API 设计\n\n## 快速开始\n\n```bash\ngit clone https://github.com/example/spring-boot-crud.git\ncd spring-boot-crud\nmvn spring-boot:run\n```',
  },
  {
    title: 'src',
    key: 'src',
    children: [
      {
        title: 'UserController.java',
        key: 'UserController.java',
        isLeaf: true,
        content: 'package com.example.controller;\n\nimport com.example.entity.User;\nimport com.example.service.UserService;\nimport org.springframework.web.bind.annotation.*;\nimport java.util.List;\n\n@RestController\n@RequestMapping("/api/users")\npublic class UserController {\n\n    private final UserService userService;\n\n    public UserController(UserService userService) {\n        this.userService = userService;\n    }\n\n    @GetMapping\n    public List<User> list() {\n        return userService.list();\n    }\n\n    @GetMapping("/{id}")\n    public User getById(@PathVariable Long id) {\n        return userService.getById(id);\n    }\n\n    @PostMapping\n    public User create(@RequestBody User user) {\n        return userService.save(user);\n    }\n\n    @PutMapping("/{id}")\n    public User update(@PathVariable Long id, @RequestBody User user) {\n        user.setId(id);\n        return userService.update(user);\n    }\n\n    @DeleteMapping("/{id}")\n    public void delete(@PathVariable Long id) {\n        userService.deleteById(id);\n    }\n}',
      },
      {
        title: 'UserService.java',
        key: 'UserService.java',
        isLeaf: true,
        content: 'package com.example.service;\n\nimport com.example.entity.User;\nimport com.example.repository.UserRepository;\nimport org.springframework.stereotype.Service;\nimport java.util.List;\n\n@Service\npublic class UserService {\n\n    private final UserRepository userRepository;\n\n    public UserService(UserRepository userRepository) {\n        this.userRepository = userRepository;\n    }\n\n    public List<User> list() {\n        return userRepository.findAll();\n    }\n\n    public User getById(Long id) {\n        return userRepository.findById(id).orElseThrow();\n    }\n\n    public User save(User user) {\n        return userRepository.save(user);\n    }\n\n    public User update(User user) {\n        return userRepository.save(user);\n    }\n\n    public void deleteById(Long id) {\n        userRepository.deleteById(id);\n    }\n}',
      },
      {
        title: 'UserRepository.java',
        key: 'UserRepository.java',
        isLeaf: true,
        content: 'package com.example.repository;\n\nimport com.example.entity.User;\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\n\n@Repository\npublic interface UserRepository extends JpaRepository<User, Long> {\n}',
      },
      {
        title: 'User.java',
        key: 'User.java',
        isLeaf: true,
        content: 'package com.example.entity;\n\nimport jakarta.persistence.*;\nimport lombok.Data;\n\n@Data\n@Entity\n@Table(name = "users")\npublic class User {\n\n    @Id\n    @GeneratedValue(strategy = GenerationType.IDENTITY)\n    private Long id;\n\n    @Column(nullable = false)\n    private String username;\n\n    @Column(nullable = false)\n    private String email;\n\n    private String phone;\n\n    private Integer status;\n}',
      },
    ],
  },
  {
    title: 'config',
    key: 'config',
    children: [
      {
        title: 'application.yml',
        key: 'application.yml',
        isLeaf: true,
        content: 'spring:\n  datasource:\n    url: jdbc:mysql://localhost:3306/your_database\n    username: root\n    password: root\n    driver-class-name: com.mysql.cj.jdbc.Driver\n  jpa:\n    hibernate:\n      ddl-auto: update\n    show-sql: true\n\nserver:\n  port: 8080',
      },
    ],
  },
  {
    title: 'pom.xml',
    key: 'pom.xml',
    isLeaf: true,
    content: '<?xml version="1.0" encoding="UTF-8"?>\n<project xmlns="http://maven.apache.org/POM/4.0.0">\n    <modelVersion>4.0.0</modelVersion>\n    <parent>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-starter-parent</artifactId>\n        <version>3.2.0</version>\n    </parent>\n    <groupId>com.example</groupId>\n    <artifactId>spring-boot-crud</artifactId>\n    <version>1.0.0</version>\n\n    <dependencies>\n        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-web</artifactId>\n        </dependency>\n        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-data-jpa</artifactId>\n        </dependency>\n        <dependency>\n            <groupId>com.mysql</groupId>\n            <artifactId>mysql-connector-j</artifactId>\n            <scope>runtime</scope>\n        </dependency>\n        <dependency>\n            <groupId>org.projectlombok</groupId>\n            <artifactId>lombok</artifactId>\n            <optional>true</optional>\n        </dependency>\n    </dependencies>\n</project>',
  },
]

const treeRef = ref()
const treeData = ref<TreeNodeData[]>([])
const selectedKeys = ref<string[]>([])
const currentFile = ref<FileNode>()

// 扁平化文件列表，用于查找
const flatFiles = ref<FileNode[]>([])
const flattenTree = (nodes: FileNode[]) => {
  const result: FileNode[] = []
  const traverse = (items: FileNode[]) => {
    for (const item of items) {
      if (item.isLeaf) {
        result.push(item)
      }
      if (item.children) {
        traverse(item.children)
      }
    }
  }
  traverse(nodes)
  return result
}

const checkFileType = (title: string, type: string) => {
  return title.endsWith(type)
}

const codeType = computed<'javascript' | 'vue'>(() => {
  const title = currentFile.value?.title || ''
  if (title.endsWith('.vue')) return 'vue'
  return 'javascript'
})

const onSelectFile = (keys: (string | number)[]) => {
  const key = keys[0] as string
  if (!key) return
  const file = flatFiles.value.find((f) => f.key === key)
  if (file) {
    currentFile.value = file
    selectedKeys.value = [key]
  } else {
    // 点击的是文件夹，展开/折叠
    const expandedKeys = treeRef.value?.getExpandedNodes?.()?.map((node: TreeNodeData) => node.key) || []
    treeRef.value?.expandNode?.(key, !expandedKeys.includes(key))
  }
}

const onCopy = () => {
  if (currentFile.value?.content) {
    copy(currentFile.value.content)
  }
}

watch(copied, () => {
  if (copied.value) {
    Message.success('复制成功')
  }
})

const onDownload = () => {
  Message.info('下载功能开发中')
}

const onOpen = (item: TemplateItem) => {
  templateData.value = item
  // 计算图标样式
  let hash = 0
  for (let i = 0; i < item.icon.length; i++) {
    hash = item.icon.charCodeAt(i) + ((hash << 5) - hash)
  }
  const index = Math.abs(hash) % iconGradients.length
  iconStyle.value = { background: iconGradients[index] }

  // TODO: 根据模板 ID 加载真实文件树，目前使用 Mock 数据
  treeData.value = mockTreeData
  flatFiles.value = flattenTree(mockTreeData)
  // 默认选中第一个文件
  if (flatFiles.value.length > 0) {
    currentFile.value = flatFiles.value[0]
    selectedKeys.value = [flatFiles.value[0].key]
  }
  visible.value = true
  nextTick(() => {
    treeRef.value?.expandAll?.(true)
  })
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
</style>
