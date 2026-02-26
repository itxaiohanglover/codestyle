<template>
  <GiPageLayout :margin="false" :body-style="{ padding: 0 }">
    <div class="gi_page template-list">
      <!-- 搜索区 -->
      <div class="search-section">
        <a-radio-group v-model="activeTab" type="button" @change="onTabChange">
          <a-radio value="all">全部模板</a-radio>
          <a-radio value="favorites">我的收藏</a-radio>
        </a-radio-group>
        <a-input-search
          v-model="queryForm.keyword"
          placeholder="搜索模板名称、描述、作者..."
          style="max-width: 600px; flex: 1"
          allow-clear
          @search="getDataList"
        />
        <a-button v-permission="['template:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          新增模板
        </a-button>
        <a-button v-permission="['template:delete']" :disabled="!selectedIds.length" type="text" status="danger" @click="onBatchDelete">
          <template #icon><icon-delete /></template>
          批量删除 {{ selectedIds.length ? `(${selectedIds.length})` : '' }}
        </a-button>
      </div>

      <!-- 模板网格 -->
      <a-spin :loading="loading" style="width: 100%">
        <div class="templates-grid">
          <div
            v-for="item in dataList"
            :key="item.id"
            class="template-card"
            :class="{ selected: selectedIds.includes(item.id) }"
            @click="onPreview(item)"
          >
            <a-checkbox
              v-permission="['template:delete']"
              class="card-checkbox"
              :model-value="selectedIds.includes(item.id)"
              @click.stop
              @change="(v: boolean) => onToggleSelect(item.id, v)"
            />
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
              <div class="template-actions">
                <a-button
                  v-permission="['template:update']"
                  type="text"
                  size="small"
                  @click.stop="onEdit(item)"
                >
                  <icon-edit />
                </a-button>
                <a-popconfirm
                  content="确定删除该模板吗？"
                  type="warning"
                  @ok="onDelete(item)"
                >
                  <a-button
                    v-permission="['template:delete']"
                    type="text"
                    size="small"
                    status="danger"
                    @click.stop
                  >
                    <icon-delete />
                  </a-button>
                </a-popconfirm>
                <a-button
                  v-permission="['template:favorite']"
                  type="text"
                  class="favorite-btn" :class="[{ active: item.isFavorite }]"
                  @click.stop="onToggleFavorite(item)"
                >
                  <icon-star-fill v-if="item.isFavorite" />
                  <icon-star v-else />
                </a-button>
              </div>
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
    <!-- 新增/修改弹窗 -->
    <AddModal ref="AddModalRef" @save-success="getDataList" />
  </GiPageLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import PreviewModal from './PreviewModal.vue'
import AddModal from './AddModal.vue'
import { usePagination } from '@/hooks'
import { type TemplateItem, type TemplateQuery, deleteTemplate, listFavoriteTemplates, listTemplate, toggleFavorite } from '@/apis/template'

defineOptions({ name: 'TemplateList' })

const activeTab = ref('all')
const queryForm = reactive<TemplateQuery>({})
const loading = ref(false)
const dataList = ref<TemplateItem[]>([])
const selectedIds = ref<number[]>([])
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

const { pagination, setTotal } = usePagination(() => getDataList())

const onTabChange = () => {
  pagination.current = 1
  selectedIds.value = []
  getDataList()
}

async function getDataList() {
  try {
    loading.value = true
    const apiFn = activeTab.value === 'favorites' ? listFavoriteTemplates : listTemplate
    const res = await apiFn({
      ...queryForm,
      page: pagination.current,
      size: pagination.pageSize,
    })
    dataList.value = res.data.list
    setTotal(res.data.total)
  } finally {
    loading.value = false
  }
}

// 切换收藏
const onToggleFavorite = async (item: TemplateItem) => {
  try {
    const res = await toggleFavorite(item.id)
    item.isFavorite = res.data
    Message.success(item.isFavorite ? '已收藏' : '已取消收藏')
    if (!item.isFavorite && activeTab.value === 'favorites') {
      getDataList()
    }
  } catch {
    Message.error('操作失败')
  }
}

// 预览
const PreviewModalRef = ref<InstanceType<typeof PreviewModal>>()
const onPreview = (item: TemplateItem) => {
  PreviewModalRef.value?.onOpen(item)
}

const AddModalRef = ref<InstanceType<typeof AddModal>>()
const onAdd = () => {
  AddModalRef.value?.onAdd()
}
const onEdit = (item: TemplateItem) => {
  AddModalRef.value?.onUpdate(item.id)
}
const onDelete = async (item: TemplateItem) => {
  try {
    await deleteTemplate(item.id)
    Message.success('删除成功')
    selectedIds.value = selectedIds.value.filter((id) => id !== item.id)
    getDataList()
  } catch {
    Message.error('删除失败')
  }
}

const onToggleSelect = (id: number, checked: boolean) => {
  if (checked) {
    selectedIds.value.push(id)
  } else {
    selectedIds.value = selectedIds.value.filter((i) => i !== id)
  }
}

const onBatchDelete = async () => {
  if (!selectedIds.value.length) return
  try {
    await deleteTemplate(selectedIds.value)
    Message.success(`已删除 ${selectedIds.value.length} 个模板`)
    selectedIds.value = []
    getDataList()
  } catch {
    Message.error('批量删除失败')
  }
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
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
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

  &.selected {
    border-color: rgb(var(--primary-6));
    background: rgba(var(--primary-1), 0.04);
  }
}

.card-checkbox {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
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

.template-actions {
  display: flex;
  align-items: center;
  gap: 2px;
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
