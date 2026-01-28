<template>
  <a-card
    :bordered="true"
    size="small"
    class="card-block add-card"
    :class="{ 'card-large': type === 2 }"
    @click="onAdd"
  >
    <div class="content">
      <div class="add-icon">
        <icon-plus />
      </div>
      <div class="description">点击创建{{ type === 1 ? '本地存储' : '对象存储' }}</div>
    </div>
  </a-card>

  <AddModal ref="AddModalRef" @save-success="search" />
</template>

<script lang="ts" setup>
import AddModal from '../AddModal.vue'

const props = defineProps({
  type: {
    type: Number,
    default: 1,
  },
})

const emit = defineEmits<{
  (e: 'save-success'): void
}>()

const search = () => {
  emit('save-success')
}

const AddModalRef = ref<InstanceType<typeof AddModal>>()
// 新增
const onAdd = () => {
  AddModalRef.value?.onAdd(props.type)
}
</script>

<style scoped lang="less">
.card-block {
  margin-bottom: 16px;

  :deep(.arco-card-header) {
    border-bottom: none;
    height: auto;
    padding: 16px;
    padding-bottom: 0;
  }

  :deep(.arco-descriptions-item-value) {
    color: var(--color-text-2);
    padding-left: 6px;
  }

  .content {
    height: 80px;
  }
}

.add-card {
  text-align: center;
  cursor: pointer;

  .add-icon {
    font-size: 22px;
  }

  .description {
    margin-top: 16px;
    color: var(--color-text-3);
    font-weight: 400;
  }

  :deep(.arco-card-body) {
    padding-top: 55px;
    padding-bottom: 53px;
  }
}

.card-large {
  :deep(.arco-card-body) {
    padding-top: 75px;
    padding-bottom: 63px;
  }
}
</style>
