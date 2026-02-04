<template>
  <a-card class="general-card" title="Skill/MCP 调用记录">
    <template #extra>
      <a-link>查看全部 →</a-link>
    </template>
    <a-table :data="tableData" :pagination="false" :bordered="false" size="medium">
      <template #columns>
        <a-table-column title="类型" data-index="type" :width="100">
          <template #cell="{ record }">
            <a-tag :color="record.type === 'MCP' ? 'arcoblue' : 'cyan'" size="small">
              {{ record.type }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="名称" data-index="name" />
        <a-table-column title="调用者" data-index="caller" :width="100" />
        <a-table-column title="状态" data-index="status" :width="100">
          <template #cell="{ record }">
            <a-badge :status="getStatusType(record.status)" :text="record.statusText" />
          </template>
        </a-table-column>
        <a-table-column title="耗时" data-index="duration" :width="100" />
        <a-table-column title="时间" data-index="time" :width="100" />
      </template>
    </a-table>
  </a-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface CallRecord {
  type: string
  name: string
  caller: string
  status: string
  statusText: string
  duration: string
  time: string
}

const tableData = ref<CallRecord[]>([
  {
    type: 'MCP',
    name: 'code-generator-java',
    caller: 'Agent-01',
    status: 'success',
    statusText: '成功',
    duration: '156ms',
    time: '14:32:15'
  },
  {
    type: 'Skill',
    name: 'knowledge-search',
    caller: 'Agent-02',
    status: 'success',
    statusText: '成功',
    duration: '89ms',
    time: '14:31:42'
  },
  {
    type: 'MCP',
    name: 'template-render',
    caller: 'Agent-01',
    status: 'processing',
    statusText: '执行中',
    duration: '-',
    time: '14:31:20'
  },
  {
    type: 'Skill',
    name: 'code-review',
    caller: 'Agent-03',
    status: 'success',
    statusText: '成功',
    duration: '234ms',
    time: '14:30:55'
  },
  {
    type: 'MCP',
    name: 'code-generator-vue',
    caller: 'Agent-02',
    status: 'error',
    statusText: '失败',
    duration: '1.52s',
    time: '14:30:12'
  }
])

const getStatusType = (status: string) => {
  const statusMap: Record<string, 'success' | 'processing' | 'danger' | 'warning' | 'normal'> = {
    success: 'success',
    processing: 'processing',
    error: 'danger'
  }
  return statusMap[status] || 'normal'
}
</script>

<style scoped lang="scss">
:deep(.arco-table-th) {
  background-color: var(--color-fill-1);
}
</style>
