<template>
  <div class="template-card" :class="{ selected }" @click="$emit('click')">
    <a-checkbox
      v-if="showCheckbox"
      class="card-checkbox"
      :model-value="selected"
      @click.stop
      @change="(v: boolean) => $emit('toggle-select', v)"
    />
    <div v-if="visibilityBadge != null" class="visibility-badge" :class="visibilityBadge === 1 ? 'public' : 'private'">
      {{ visibilityBadge === 1 ? '公开' : '私有' }}
    </div>
    <div class="template-header">
      <div class="template-icon" :style="iconStyle">
        {{ item.icon || item.name?.substring(0, 2) }}
      </div>
      <div class="template-info">
        <div class="template-name">{{ item.name }}</div>
        <div class="template-author">{{ item.author }}</div>
      </div>
    </div>
    <div class="template-description">{{ item.description }}</div>
    <div class="template-tags">
      <a-tag v-for="tag in item.tags" :key="tag.label" :color="tag.color" size="small">{{ tag.label }}</a-tag>
    </div>
    <div class="template-footer">
      <div class="template-stats">
        <span class="stat-item"><icon-download /> {{ item.downloadCount }}</span>
        <span class="stat-item"><icon-star-fill /> {{ item.rating }}</span>
      </div>
      <div class="template-actions">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TemplateItem } from '@/apis/template/type'

const props = defineProps<{
  item: TemplateItem
  selected?: boolean
  showCheckbox?: boolean
  visibilityBadge?: number | null
}>()

defineEmits<{
  (e: 'click'): void
  (e: 'toggle-select', v: boolean): void
}>()

const GRADIENTS = [
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

const iconStyle = computed(() => {
  const icon = props.item.icon || ''
  let hash = 0
  for (let i = 0; i < icon.length; i++) hash = icon.charCodeAt(i) + ((hash << 5) - hash)
  return { background: GRADIENTS[Math.abs(hash) % GRADIENTS.length] }
})
</script>

<style scoped lang="scss">
.template-card {
  background: var(--color-bg-2);
  border-radius: $radius-box;
  border: 1px solid var(--color-border);
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  &:hover { border-color: $color-primary; box-shadow: 0 4px 12px rgba(var(--primary-6), 0.1); transform: translateY(-2px); }
  &.selected { border-color: rgb(var(--primary-6)); background: rgba(var(--primary-1), 0.04); }
}
.card-checkbox { position: absolute; top: 10px; right: 10px; z-index: 1; }
.visibility-badge {
  position: absolute; top: 12px; left: 12px; font-size: 12px; padding: 2px 8px; border-radius: 4px; font-weight: 500;
  &.public { background: rgba(var(--green-1), 0.8); color: rgb(var(--green-6)); }
  &.private { background: rgba(var(--orange-1), 0.8); color: rgb(var(--orange-6)); }
}
.template-header { display: flex; align-items: flex-start; gap: 16px; margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid var(--color-border-1); }
.template-icon { width: 52px; height: 52px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 16px; font-weight: 600; flex-shrink: 0; }
.template-info { flex: 1; min-width: 0; }
.template-name { font-size: 15px; font-weight: 600; color: var(--color-text-1); margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.template-author { font-size: 13px; color: var(--color-text-3); }
.template-description { font-size: 14px; color: var(--color-text-2); line-height: 1.6; margin-bottom: 14px; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.template-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.template-footer { display: flex; align-items: center; justify-content: space-between; }
.template-actions { display: flex; align-items: center; gap: 2px; }
.template-stats { display: flex; align-items: center; gap: 16px; }
.stat-item { display: flex; align-items: center; gap: 5px; font-size: 13px; color: var(--color-text-3); font-weight: 500; }
</style>
