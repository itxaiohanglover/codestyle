<template>
  <GiPageLayout :margin="false" :body-style="{ padding: 0 }">
    <div class="gi_page template-mine">
      <div class="search-section">
        <a-radio-group v-model="activeTab" type="button" @change="onTabChange">
          <a-radio value="mine">我的模板</a-radio>
          <a-radio value="favorites">我的收藏</a-radio>
        </a-radio-group>
        <a-input-search
          v-model="queryForm.keyword"
          placeholder="搜索模板名称、描述..."
          style="max-width: 600px; flex: 1"
          allow-clear
          @search="getDataList"
        />
        <a-button v-if="activeTab === 'mine'" v-permission="['template:mine:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          新增模板
        </a-button>
        <a-button v-if="activeTab === 'mine'" v-permission="['template:mine:delete']" :disabled="!selectedIds.length" type="text" status="danger" @click="onBatchDelete">
          <template #icon><icon-delete /></template>
          批量删除 {{ selectedIds.length ? `(${selectedIds.length})` : '' }}
        </a-button>
      </div>

      <a-spin :loading="loading" style="width: 100%">
        <div class="templates-grid">
          <TemplateCard
            v-for="item in dataList"
            :key="item.id"
            :item="item"
            :selected="selectedIds.includes(item.id)"
            show-checkbox
            :visibility-badge="activeTab === 'mine' ? item.visibility : null"
            @click="onPreview(item)"
            @toggle-select="(v) => onToggleSelect(item.id, v)"
          >
            <template #actions>
              <a-tooltip v-if="activeTab === 'mine'" :content="item.visibility === 1 ? '设为私有' : '公开到模板库'">
                <a-button v-permission="['template:mine:publish']" type="text" size="small" :status="item.visibility === 1 ? 'warning' : 'success'" @click.stop="onToggleVisibility(item)">
                  <icon-eye v-if="item.visibility === 1" />
                  <icon-eye-invisible v-else />
                </a-button>
              </a-tooltip>
              <a-button v-if="activeTab === 'mine'" v-permission="['template:mine:update']" type="text" size="small" @click.stop="onEdit(item)">
                <icon-edit />
              </a-button>
              <a-popconfirm v-if="activeTab === 'mine'" content="确定删除该模板吗？" type="warning" @ok="onDelete(item)">
                <a-button v-permission="['template:mine:delete']" type="text" size="small" status="danger" @click.stop>
                  <icon-delete />
                </a-button>
              </a-popconfirm>
              <a-button v-if="activeTab === 'favorites'" type="text" class="favorite-btn active" @click.stop="onToggleFavorite(item)">
                <icon-star-fill />
              </a-button>
            </template>
          </TemplateCard>
        </div>

        <a-empty v-if="!loading && dataList.length === 0" description="暂无模板，点击「新增模板」开始创建" />
      </a-spin>

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
import PreviewModal from '../list/PreviewModal.vue'
import AddModal from '../list/AddModal.vue'
import TemplateCard from '../components/TemplateCard.vue'
import { usePagination } from '@/hooks'
import { type TemplateItem, type TemplateQuery, deleteTemplate, listFavoriteTemplates, listMyTemplates, toggleFavorite, toggleVisibility } from '@/apis/template'

defineOptions({ name: 'TemplateMine' })

const activeTab = ref('mine')
const queryForm = reactive<TemplateQuery>({})
const loading = ref(false)
const dataList = ref<TemplateItem[]>([])
const selectedIds = ref<number[]>([])

const { pagination, setTotal } = usePagination(() => getDataList())

const onTabChange = () => {
  pagination.current = 1
  selectedIds.value = []
  getDataList()
}

async function getDataList() {
  try {
    loading.value = true
    const apiFn = activeTab.value === 'favorites' ? listFavoriteTemplates : listMyTemplates
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

const onToggleVisibility = async (item: TemplateItem) => {
  try {
    await toggleVisibility(item.id)
    item.visibility = item.visibility === 1 ? 0 : 1
    Message.success(item.visibility === 1 ? '已公开到模板库' : '已设为私有')
  } catch {
    Message.error('操作失败')
  }
}

const onToggleFavorite = async (item: TemplateItem) => {
  try {
    await toggleFavorite(item.id)
    Message.success('已取消收藏')
    getDataList()
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
.template-mine { padding: 20px; }
.search-section { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.templates-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; margin-bottom: 24px; }
.favorite-btn { color: var(--color-text-4); font-size: 18px; padding: 4px; &.active { color: rgb(var(--warning-6)); } }
.pagination-wrapper { display: flex; justify-content: center; padding: 16px 0; }
</style>
