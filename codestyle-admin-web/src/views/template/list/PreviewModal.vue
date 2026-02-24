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
                  <div class="stat-value">{{ templateData?.starCount ?? 0 }}</div>
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
                <span class="version-number">{{ templateData?.version || '-' }}</span>
              </div>

              <!-- 元数据 -->
              <div class="meta-section">
                <div class="meta-item">
                  <div class="meta-icon"><icon-user /></div>
                  <div class="meta-content">
                    <div class="meta-label">创建者</div>
                    <div class="meta-value">{{ templateData?.createUserString || templateData?.author }}</div>
                  </div>
                </div>
                <div class="meta-item">
                  <div class="meta-icon"><icon-clock-circle /></div>
                  <div class="meta-content">
                    <div class="meta-label">创建时间</div>
                    <div class="meta-value">{{ templateData?.createTime || '-' }}</div>
                  </div>
                </div>
                <div class="meta-item">
                  <div class="meta-icon"><icon-calendar /></div>
                  <div class="meta-content">
                    <div class="meta-label">更新时间</div>
                    <div class="meta-value">{{ templateData?.updateTime || '-' }}</div>
                  </div>
                </div>
              </div>

              <!-- 下载按钮 -->
              <a-button type="primary" long @click="onDownload">
                <template #icon><icon-download /></template>
                下载模板
              </a-button>

              <div v-if="templateData?.groupId || templateData?.artifactId" class="info-section">
                <div class="section-title">坐标信息</div>
                <div class="section-text">
                  <div v-if="templateData?.groupId">GroupId: {{ templateData.groupId }}</div>
                  <div v-if="templateData?.artifactId">ArtifactId: {{ templateData.artifactId }}</div>
                </div>
              </div>
            </div>
          </a-scrollbar>
        </a-layout-sider>

        <!-- 模板描述区 -->
        <a-layout-content>
          <a-card :bordered="false">
            <template #title>
              <a-typography-title :heading="6" ellipsis>模板详情</a-typography-title>
            </template>
            <template #extra>
              <a-link @click="onCopy">
                <template #icon><icon-copy /></template>
                复制下载链接
              </a-link>
            </template>
            <a-scrollbar style="height: 650px; overflow: auto">
              <div class="template-description">
                <p>{{ templateData?.description }}</p>
              </div>
            </a-scrollbar>
          </a-card>
        </a-layout-content>
      </a-layout>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useClipboard } from '@vueuse/core'
import type { TemplateDetail, TemplateItem } from '@/apis/template'
import { getTemplateDetail, recordDownload } from '@/apis/template'

const { copy, copied } = useClipboard()

const visible = ref(false)
const templateData = ref<TemplateDetail>()
const iconStyle = ref<Record<string, string>>({})
const detailLoading = ref(false)

// 图标渐变色
const iconGradients = [
  'linear-gradient(135deg, #0891B2 0%, #22D3EE 100%)',
  'linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)',
  'linear-gradient(135deg, #059669 0%, #34D399 100%)',
  'linear-gradient(135deg, #DC2626 0%, #F87171 100%)',
  'linear-gradient(135deg, #D97706 0%, #FBBF24 100%)',
  'linear-gradient(135deg, #2563EB 0%, #60A5FA 100%)',
]

const onCopy = () => {
  if (templateData.value?.downloadUrl) {
    copy(templateData.value.downloadUrl)
  }
}

watch(copied, () => {
  if (copied.value) {
    Message.success('复制成功')
  }
})

const onDownload = async () => {
  if (!templateData.value) return
  try {
    await recordDownload(templateData.value.id)
    if (templateData.value.downloadUrl) {
      window.open(templateData.value.downloadUrl, '_blank')
    }
    Message.success('下载成功')
  } catch {
    Message.error('下载失败')
  }
}

const onOpen = async (item: TemplateItem) => {
  templateData.value = item as TemplateDetail
  // 计算图标样式
  let hash = 0
  for (let i = 0; i < item.icon.length; i++) {
    hash = item.icon.charCodeAt(i) + ((hash << 5) - hash)
  }
  const index = Math.abs(hash) % iconGradients.length
  iconStyle.value = { background: iconGradients[index] }

  visible.value = true
  detailLoading.value = true

  try {
    const res = await getTemplateDetail(item.id)
    templateData.value = res.data
  } catch {
    Message.error('获取模板详情失败')
  } finally {
    detailLoading.value = false
  }
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
