<template>
  <GiPageLayout :margin="false" :body-style="{ padding: 0 }">
    <div class="gi_page template-list">
      <!-- 搜索区 -->
      <div class="search-section">
        <a-input-search
          v-model="queryForm.keyword"
          placeholder="搜索模板名称、描述、作者..."
          style="max-width: 600px"
          allow-clear
          @search="getDataList"
        />
      </div>

      <!-- 模板网格 -->
      <a-spin :loading="loading" style="width: 100%">
        <div class="templates-grid">
          <div
            v-for="item in dataList"
            :key="item.id"
            class="template-card"
            @click="onPreview(item)"
          >
            <div class="template-header">
              <div class="template-icon" :style="getIconStyle(item.icon)">
                {{ item.icon || item.name?.substring(0, 2) }}
              </div>
              <div class="template-info">
                <div class="template-name">{{ item.name }}</div>
                <div class="template-author">{{ item.author }}</div>
              </div>
            </div>
            <div class="template-description">{{ item.description }}</div>
            <div class="template-tags">
              <a-tag v-for="tag in item.tags" :key="tag.label" :color="tag.color" size="small">
                {{ tag.label }}
              </a-tag>
            </div>
            <div class="template-footer">
              <div class="template-stats">
                <span class="stat-item">
                  <icon-download />
                  {{ item.downloadCount }}
                </span>
                <span class="stat-item">
                  <icon-star-fill />
                  {{ item.rating }}
                </span>
              </div>
              <a-button
                type="text"
                :class="['favorite-btn', { active: item.isFavorite }]"
                @click.stop="onToggleFavorite(item)"
              >
                <icon-star-fill v-if="item.isFavorite" />
                <icon-star v-else />
              </a-button>
            </div>
          </div>
        </div>

        <a-empty v-if="!loading && dataList.length === 0" />
      </a-spin>

      <!-- 分页 -->
      <div v-if="dataList.length > 0" class="pagination-wrapper">
        <a-pagination
          v-bind="pagination"
        />
      </div>
    </div>

    <!-- 预览弹窗 -->
    <PreviewModal ref="PreviewModalRef" />
  </GiPageLayout>
</template>

<script setup lang="ts">
import PreviewModal from './PreviewModal.vue'
import { usePagination } from '@/hooks'
defineOptions({ name: 'TemplateList' })

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

const queryForm = reactive<{ keyword?: string }>({})
const loading = ref(false)
const dataList = ref<TemplateItem[]>([])
const { pagination } = usePagination(() => getDataList())

// 图标渐变色列表
const iconGradients = [
  'linear-gradient(135deg, #0891B2 0%, #22D3EE 100%)',
  'linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)',
  'linear-gradient(135deg, #059669 0%, #34D399 100%)',
  'linear-gradient(135deg, #DC2626 0%, #F87171 100%)',
  'linear-gradient(135deg, #D97706 0%, #FBBF24 100%)',
  'linear-gradient(135deg, #2563EB 0%, #60A5FA 100%)',
  'linear-gradient(135deg, #DB2777 0%, #F472B6 100%)',
  'linear-gradient(135deg, #4F46E5 0%, #818CF8 100%)',
  'linear-gradient(135deg, #0D9488 0%, #2DD4BF 100%)',
]

const getIconStyle = (icon: string) => {
  let hash = 0
  for (let i = 0; i < icon.length; i++) {
    hash = icon.charCodeAt(i) + ((hash << 5) - hash)
  }
  const index = Math.abs(hash) % iconGradients.length
  return { background: iconGradients[index] }
}

// Mock 数据
const mockData: TemplateItem[] = [
  {
    id: 1,
    name: 'Spring Boot CRUD',
    icon: 'SB',
    author: 'Charles7c',
    description: '标准的 Spring Boot 增删改查模板，包含 Controller、Service、Repository 完整代码结构，支持分页和高级查询。',
    tags: [{ label: 'Java', color: 'blue' }, { label: 'Spring Boot', color: 'green' }, { label: '后端', color: 'purple' }],
    downloadCount: '1,892',
    rating: 4.8,
    isFavorite: true,
  },
  {
    id: 2,
    name: 'Vue3 组件模板',
    icon: 'V3',
    author: '官方团队',
    description: 'Vue 3 Composition API 组件模板，包含 TypeScript 类型定义、Props、Emits、生命周期钩子等完整结构。',
    tags: [{ label: 'Vue3', color: 'green' }, { label: 'TypeScript', color: 'blue' }, { label: '前端', color: 'purple' }],
    downloadCount: '1,543',
    rating: 4.9,
    isFavorite: true,
  },
  {
    id: 3,
    name: 'MyBatis Mapper',
    icon: 'MB',
    author: '张三',
    description: 'MyBatis Mapper 映射文件模板，包含常用的 SQL 语句、动态查询、结果映射等配置，支持 MyBatis-Plus。',
    tags: [{ label: 'MyBatis', color: 'blue' }, { label: '数据库', color: 'green' }],
    downloadCount: '987',
    rating: 4.7,
    isFavorite: false,
  },
  {
    id: 4,
    name: 'RESTful API',
    icon: 'API',
    author: '李四',
    description: '标准的 RESTful API 接口模板，包含统一响应格式、异常处理、参数校验、Swagger 文档注解等。',
    tags: [{ label: 'API', color: 'blue' }, { label: 'Spring Boot', color: 'green' }, { label: '后端', color: 'purple' }],
    downloadCount: '1,234',
    rating: 4.6,
    isFavorite: false,
  },
  {
    id: 5,
    name: '表单页面',
    icon: 'FM',
    author: '王五',
    description: '基于 Arco Design 的表单页面模板，包含表单验证、动态表单、级联选择、文件上传等常用功能。',
    tags: [{ label: 'Vue3', color: 'green' }, { label: 'Arco Design', color: 'blue' }, { label: '前端', color: 'purple' }],
    downloadCount: '1,765',
    rating: 4.8,
    isFavorite: true,
  },
  {
    id: 6,
    name: 'JWT 认证',
    icon: 'JWT',
    author: '赵六',
    description: 'JWT Token 认证鉴权模板，包含用户登录、Token 生成与验证、权限控制、刷新机制等完整实现。',
    tags: [{ label: 'Security', color: 'blue' }, { label: 'JWT', color: 'green' }, { label: '后端', color: 'purple' }],
    downloadCount: '1,456',
    rating: 4.9,
    isFavorite: false,
  },
  {
    id: 7,
    name: '数据报表',
    icon: 'RPT',
    author: '孙七',
    description: '数据报表展示模板，集成 ECharts 图表库，包含折线图、柱状图、饼图等常用图表及数据导出功能。',
    tags: [{ label: 'Vue3', color: 'green' }, { label: 'ECharts', color: 'blue' }, { label: '可视化', color: 'purple' }],
    downloadCount: '1,123',
    rating: 4.7,
    isFavorite: false,
  },
  {
    id: 8,
    name: '定时任务',
    icon: 'TSK',
    author: '周八',
    description: 'Spring Boot 定时任务模板，支持 Cron 表达式配置、任务动态管理、执行记录查询、失败重试等功能。',
    tags: [{ label: 'Spring Boot', color: 'blue' }, { label: 'Task', color: 'green' }, { label: '后端', color: 'purple' }],
    downloadCount: '987',
    rating: 4.5,
    isFavorite: false,
  },
  {
    id: 9,
    name: '消息推送',
    icon: 'WS',
    author: '官方团队',
    description: '实时消息推送模板，支持 WebSocket 长连接、消息队列、多端同步、离线消息存储等功能。',
    tags: [{ label: 'WebSocket', color: 'blue' }, { label: 'Redis', color: 'green' }, { label: '实时通信', color: 'purple' }],
    downloadCount: '1,543',
    rating: 4.8,
    isFavorite: true,
  },
]

// 查询列表（目前使用 Mock 数据，后续替换为 API 调用）
const getDataList = async () => {
  try {
    loading.value = true
    // TODO: 替换为真实 API 调用
    // const { data } = await listTemplate(queryForm)
    await new Promise((resolve) => setTimeout(resolve, 300))
    let list = [...mockData]
    if (queryForm.keyword) {
      const keyword = queryForm.keyword.toLowerCase()
      list = list.filter(
        (item) =>
          item.name.toLowerCase().includes(keyword)
          || item.description.toLowerCase().includes(keyword)
          || item.author.toLowerCase().includes(keyword),
      )
    }
    dataList.value = list
    pagination.total = list.length
  } finally {
    loading.value = false
  }
}

// 切换收藏
const onToggleFavorite = (item: TemplateItem) => {
  item.isFavorite = !item.isFavorite
}

// 预览
const PreviewModalRef = ref<InstanceType<typeof PreviewModal>>()
const onPreview = (item: TemplateItem) => {
  PreviewModalRef.value?.onOpen(item)
}

onMounted(() => {
  getDataList()
})
</script>

<style scoped lang="scss">
.template-list {
  padding: 20px;
}

.search-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.template-card {
  background: var(--color-bg-2);
  border-radius: $radius-box;
  border: 1px solid var(--color-border);
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;

  &:hover {
    border-color: $color-primary;
    box-shadow: 0 4px 12px rgba(var(--primary-6), 0.1);
    transform: translateY(-2px);
  }
}

.template-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-1);
}

.template-icon {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.template-info {
  flex: 1;
  min-width: 0;
}

.template-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-author {
  font-size: 13px;
  color: var(--color-text-3);
}

.template-description {
  font-size: 14px;
  color: var(--color-text-2);
  line-height: 1.6;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.template-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.template-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.template-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--color-text-3);
  font-weight: 500;
}

.favorite-btn {
  color: var(--color-text-4);
  font-size: 18px;
  padding: 4px;

  &.active {
    color: rgb(var(--warning-6));
  }

  &:hover {
    color: rgb(var(--warning-6));
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
</style>
