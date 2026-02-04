<template>
  <a-card class="general-card" title="我的项目">
    <template #extra>
      <a-link href="https://github.com/itxaiohanglover/codestyle" target="_blank" rel="noopener">GitHub</a-link>
    </template>
    <a-row :gutter="[14, 14]">
      <a-col
        v-for="(item, index) in list"
        :key="index"
        :xs="24"
        :sm="24"
        :md="12"
        :lg="12"
        :xl="8"
        :xxl="8"
      >
        <a-card :bordered="true" hoverable>
          <div class="badge badge-right" :style="`background-color: ${item.statusColor}`">{{ item.status }}</div>
          <a-card-meta>
            <template #title>
              <a-space>
                <img :src="item.logo" width="25px" height="25px" alt="logo" />
                <a-typography-paragraph
                  :ellipsis="{
                    rows: 1,
                    showTooltip: true,
                    css: true,
                  }"
                >
                  {{ item.alias }}
                </a-typography-paragraph>
              </a-space>
            </template>
            <template #description>
              <a-typography-paragraph
                :ellipsis="{
                  rows: 2,
                  showTooltip: true,
                  css: true,
                }"
              >
                <a-typography-text type="secondary">
                  {{ item.desc }}
                </a-typography-text>
              </a-typography-paragraph>
            </template>
          </a-card-meta>
          <template #actions>
            <a-tooltip content="点个 Star 吧">
              <span class="icon-hover">
                <a :href="item.url" target="_blank" rel="noopener"><IconThumbUp :size="20" /></a>
              </span>
            </a-tooltip>
          </template>
        </a-card>
      </a-col>
    </a-row>
  </a-card>
</template>

<script setup lang="ts">
const list = [
  {
    alias: 'CodeStyle',
    name: 'codestyle',
    owner: 'itxaiohanglover',
    desc: '🔥代码规范管理系统🔥持续迭代优化的前后端分离中后台管理系统框架，开箱即用，持续提供舒适的开发体验。',
    logo: '/static/logo.png',
    url: 'https://github.com/itxaiohanglover/codestyle',
    status: '迭代',
    statusColor: 'rgb(var(--primary-6))',
  },
]
</script>

<style scoped lang="less">
:deep(.arco-card-body) {
  position: relative;
  overflow: hidden;
  .badge {
    position: absolute;
    font-size: 11px;
    height: 18px;
    line-height: 18px;
    text-align: center;
    width: 74px;
    color: #fff;
  }
  .badge-left {
    -moz-transform: rotate(-45deg);
    -ms-transform: rotate(-45deg);
    -o-transform: rotate(-45deg);
    -webkit-transform: rotate(-45deg);
    transform: rotate(-45deg);
    left: -20px;
    top: 6px;
  }
  .badge-right {
    -moz-transform: rotate(45deg);
    -ms-transform: rotate(45deg);
    -o-transform: rotate(45deg);
    -webkit-transform: rotate(45deg);
    transform: rotate(45deg);
    right: -20px;
    top: 6px;
  }
}

.icon-hover {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  color: rgba(var(--primary-6));
  border-radius: 50%;
  transition: all 0.1s;
  animation: icon-hover-animated 1.2s ease-in-out infinite;
}
.icon-hover:hover {
  background-color: rgb(var(--gray-2));
}

@keyframes icon-hover-animated {
  50% {
    transform: scale(0.8);
  }
}
</style>
