<template>
  <a-modal
    v-model:visible="visible"
    title="文件回收站"
    :width="width >= 1100 ? 1100 : '100%'"
    draggable
    :footer="false"
    @close="onClose"
  >
    <GiTable
      row-key="id"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :scroll="{ x: '100%', y: '100%', minWidth: 600 }"
      :pagination="pagination"
      :disabled-tools="['size', 'setting', 'fullscreen']"
      :disabled-column-keys="['label']"
      @refresh="search"
    >
      <template #toolbar-left>
        <a-input-search v-model="queryForm.originalName" placeholder="搜索名称" allow-clear style="width: 200px" @search="search" />
        <a-button @click="reset">
          <template #icon><icon-refresh /></template>
          <template #default>重置</template>
        </a-button>
      </template>
      <template #toolbar-right>
        <a-button v-permission="['system:fileRecycle:clean']" type="outline" status="danger" @click="onClean">
          <template #icon><icon-delete /></template>
          <template #default>清空回收站</template>
        </a-button>
      </template>
      <template #type="{ record }">{{ getFileType(record.type) }}</template>
      <template #size="{ record }">
        <span v-if="record.type === 0" v-permission="['system:file:calcDirSize']">
          <a-link v-if="record.size === null" @click="calculateDirSize(record)">计算</a-link>
          <span v-else>
            {{ formatFileSize(record.size) }}
          </span>
        </span>
        <span v-else>{{ formatFileSize(record.size) }}</span>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['system:fileRecycle:restore']" title="还原" @click="onRestore(record)">还原</a-link>
          <a-link
            v-permission="['system:fileRecycle:delete']"
            status="danger"
            title="删除"
            @click="onDelete(record)"
          >
            删除
          </a-link>
        </a-space>
      </template>
    </GiTable>
  </a-modal>
</template>

<script setup lang="ts">
import { Message, Modal, type TableInstance } from '@arco-design/web-vue'
import { useWindowSize } from '@vueuse/core/index'
import {
  type FileItem,
  type FileQuery,
  calcDirSize,
  cleanRecycleBin,
  deleteRecycleFile,
  listRecycleFiles,
  restoreRecycleFile,
} from '@/apis/system/file'
import { useTable } from '@/hooks'
import { formatFileSize, isMobile } from '@/utils'
import has from '@/utils/has'
import { FileTypeList } from '@/constant/file'

const emit = defineEmits<{
  (e: 'close'): void
}>()

const visible = ref(false)
const { width } = useWindowSize()

const queryForm = reactive<FileQuery>({
  sort: ['updateTime,desc'],
})
const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete,
} = useTable((page) => listRecycleFiles({ ...queryForm, ...page }), { immediate: false })
const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '名称', dataIndex: 'originalName', slotName: 'originalName', minWidth: 100, ellipsis: true, tooltip: true },
  { title: '类型', dataIndex: 'type', slotName: 'type', width: 100 },
  { title: '大小', dataIndex: 'size', slotName: 'size' },
  { title: '删除时间', dataIndex: 'updateTime', width: 180 },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 130,
    align: 'center',
    fixed: !isMobile() ? 'right' : undefined,
    show: has.hasPermOr(['system:fileRecycle:restore', 'system:fileRecycle:delete']),
  },
]

// 重置
const reset = () => {
  queryForm.originalName = undefined
  queryForm.type = undefined
  search()
}

// 获取文件类型
const getFileType = (type: number) => {
  if (type === 0) return '文件夹'
  return FileTypeList.find((item) => item.value === type)?.name
}

// 计算文件夹大小
const calculateDirSize = async (record: FileItem) => {
  if (record.type !== 0) return
  try {
    const { data } = await calcDirSize(record.id)
    record.size = data.size
  } catch (err) {
    Message.error('计算失败，请重试')
  }
}

// 还原
const onRestore = (record: FileItem) => {
  Modal.warning({
    title: '提示',
    content: `是否确定还原${record.type === 0 ? '文件夹' : '文件'}「${record.originalName}」？`,
    hideCancel: false,
    maskClosable: false,
    onOk: async () => {
      await restoreRecycleFile(record.id)
      Message.success('还原成功')
      search()
    },
  })
}

// 删除
const onDelete = (record: FileItem) => {
  return handleDelete(() => deleteRecycleFile(record.id), {
    content: `是否确定删除${record.type === 0 ? '文件夹' : '文件'}「${record.originalName}」？`,
    showModal: true,
  })
}

// 清空回收站
const onClean = () => {
  Modal.warning({
    title: '提示',
    content: '是否确定清空回收站？',
    hideCancel: false,
    maskClosable: false,
    onOk: async () => {
      await cleanRecycleBin()
      Message.success('清空成功')
      search()
    },
  })
}

// 关闭
const onClose = () => {
  visible.value = false
  dataList.value = []
  emit('close')
}

// 打开
const onOpen = () => {
  reset()
  visible.value = true
}

defineExpose({ onOpen })
</script>

<style scoped lang="scss"></style>
