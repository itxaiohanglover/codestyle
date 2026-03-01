<template>
  <GiPageLayout :margin="false" :body-style="{ padding: 0 }">
    <div class="gi_page template-list">
      <!-- 搜索区 -->
      <div class="search-section">
        <a-input-search
          v-model="queryForm.keyword"
          placeholder="搜索模板名称、描述、作者..."
          style="max-width: 400px; flex: 1"
          allow-clear
          @search="getDataList"
        />
        <a-select v-model="sortBy" placeholder="排序" style="width: 140px" @change="getDataList">
          <a-option value="newest">最新发布</a-option>
          <a-option value="popular">最多下载</a-option>
          <a-option value="rating">最高评分</a-option>
        </a-select>
        <a-button-group size="small">
          <a-button :type="viewMode === 'grid' ? 'primary' : 'secondary'" @click="viewMode = 'grid'"><icon-apps /></a-button>
          <a-button :type="viewMode === 'list' ? 'primary' : 'secondary'" @click="viewMode = 'list'"><icon-list /></a-button>
        </a-button-group>
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
        <div class="templates-grid" :class="{ 'list-mode': viewMode === 'list' }">
          <TemplateCard
            v-for="item in dataList"
            :key="item.id"
            :item="item"
            :selected="selectedIds.includes(item.id)"
            show-checkbox
            @click="onPreview(item)"
            @toggle-select="(v) => onToggleSelect(item.id, v)"
          >
            <template #actions>
              <a-button v-permission="['template:update']" type="text" size="small" @click.stop="onEdit(item)">
                <icon-edit />
              </a-button>
              <a-popconfirm content="确定删除该模板吗？" type="warning" @ok="onDelete(item)">
                <a-button v-permission="['template:delete']" type="text" size="small" status="danger" @click.stop>
                  <icon-delete />
                </a-button>
              </a-popconfirm>
              <a-button v-permission="['template:favorite']" type="text" class="favorite-btn" :class="[{ active: item.isFavorite }]" @click.stop="onToggleFavorite(item)">
                <icon-star-fill v-if="item.isFavorite" />
                <icon-star v-else />
              </a-button>
            </template>
          </TemplateCard>
        </div>

        <a-empty v-if="!loading && dataList.length === 0" />
      </a-spin>

      <!-- 分页 -->
      <div v-if="dataList.length > 0" class="pagination-wrapper">
        <a-pagination v-bind="pagination" />
      </div>
    </div>

    <PreviewModal ref="PreviewModalRef" />
    <AddModal ref="AddModalRef" @save-success="getDataList" />
  </GiPageLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import TemplateCard from '../components/TemplateCard.vue'
import PreviewModal from './PreviewModal.vue'
import AddModal from './AddModal.vue'
import { usePagination } from '@/hooks'
import { type TemplateItem, type TemplateQuery, deleteTemplate, listTemplate, searchTemplateQuick, toggleFavorite } from '@/apis/template'

defineOptions({ name: 'TemplateList' })

const queryForm = reactive<TemplateQuery>({})
const loading = ref(false)
const dataList = ref<TemplateItem[]>([])
const selectedIds = ref<number[]>([])
const sortBy = ref('newest')
const viewMode = ref<'grid' | 'list'>('grid')

const { pagination, setTotal } = usePagination(() => getDataList())

async function getDataList() {
  try {
    loading.value = true
    const keyword = queryForm.keyword?.trim()
    if (keyword) {
      const res = await searchTemplateQuick(keyword)
      dataList.value = res.data || []
      setTotal(dataList.value.length)
    } else {
      const res = await listTemplate({
        ...queryForm,
        sort: sortBy.value,
        page: pagination.current,
        size: pagination.pageSize,
      })
      dataList.value = res.data.list
      setTotal(res.data.total)
    }
  } finally {
    loading.value = false
  }
}

const onToggleFavorite = async (item: TemplateItem) => {
  try {
    const res = await toggleFavorite(item.id)
    item.isFavorite = res.data
    Message.success(item.isFavorite ? '已收藏' : '已取消收藏')
  } catch {
    Message.error('操作失败')
  }
}

const PreviewModalRef = ref<InstanceType<typeof PreviewModal>>()
const onPreview = (item: TemplateItem) => PreviewModalRef.value?.onOpen(item)

const AddModalRef = ref<InstanceType<typeof AddModal>>()
const onAdd = () => AddModalRef.value?.onAdd()
const onEdit = (item: TemplateItem) => AddModalRef.value?.onUpdate(item.id)

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
  if (checked) selectedIds.value.push(id)
  else selectedIds.value = selectedIds.value.filter((i) => i !== id)
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

onMounted(() => getDataList())
</script>

<style scoped lang="scss">
.template-list { padding: 20px; }
.search-section { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.templates-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; margin-bottom: 24px; &.list-mode { grid-template-columns: 1fr; } }
.favorite-btn {
  color: var(--color-text-4); font-size: 18px; padding: 4px;
  &.active { color: rgb(var(--warning-6)); }
  &:hover { color: rgb(var(--warning-6)); }
}
.pagination-wrapper { display: flex; justify-content: center; padding: 16px 0; }
</style>
