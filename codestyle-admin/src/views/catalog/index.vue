<template>
  <div style="padding: 24px 24px 48px; max-width: 1200px; margin: 0 auto">
    <a-typography-title :heading="4" :style="{ margin: '8px 0 16px' }"
      >目录</a-typography-title
    >

    <!-- 过滤器条 -->
    <div class="filter-bar">
      <a-space wrap>
        <a-dropdown trigger="click">
          <a-button type="secondary" shape="round">语言 <icon-down /></a-button>
          <template #content>
            <a-doption
              v-for="opt in langOptions"
              :key="opt"
              @click="onSelect('lang', opt)"
              >{{ opt }}</a-doption
            >
          </template>
        </a-dropdown>
        <a-dropdown trigger="click">
          <a-button type="secondary" shape="round">框架 <icon-down /></a-button>
          <template #content>
            <a-doption
              v-for="opt in frameworkOptions"
              :key="opt"
              @click="onSelect('framework', opt)"
              >{{ opt }}</a-doption
            >
          </template>
        </a-dropdown>
        <a-dropdown trigger="click">
          <a-button type="secondary" shape="round"
            >数据库 <icon-down
          /></a-button>
          <template #content>
            <a-doption
              v-for="opt in dbOptions"
              :key="opt"
              @click="onSelect('db', opt)"
              >{{ opt }}</a-doption
            >
          </template>
        </a-dropdown>
        <a-dropdown trigger="click">
          <a-button type="secondary" shape="round">场景 <icon-down /></a-button>
          <template #content>
            <a-doption
              v-for="opt in sceneOptions"
              :key="opt"
              @click="onSelect('scene', opt)"
              >{{ opt }}</a-doption
            >
          </template>
        </a-dropdown>
      </a-space>
      <div class="filter-actions">
        <a-badge :count="activeCount" dot-type="round" :max-count="99">
          <a-tag v-if="activeCount > 0" color="orangered">{{
            activeCount
          }}</a-tag>
        </a-badge>
        <a-button type="secondary" shape="round" @click="resetFilters">
          重置过滤器
          <template #icon><icon-undo /></template>
        </a-button>
      </div>
    </div>

    <!-- 卡片网格 -->
    <div class="cards">
      <a-row :gutter="16">
        <a-col
          v-for="item in filteredList"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <a-card :hoverable="true" :style="{ marginBottom: '16px' }">
            <template #cover>
              <a-image
                :src="item.cover"
                :alt="item.title"
                fit="cover"
                height="180"
              />
            </template>
            <div class="card-body">
              <div class="card-title">
                <a-avatar :size="36" :style="{ marginRight: '8px' }">{{
                  item.title.slice(0, 1)
                }}</a-avatar>
                <div>
                  <div class="name"
                    >{{ item.title
                    }}<a-tag size="small" style="margin-left: 6px"
                      >专业版</a-tag
                    ></div
                  >
                  <div class="sub">{{ item.country }}</div>
                </div>
              </div>
              <a-divider :style="{ margin: '10px 0' }" />
              <a-descriptions
                :column="1"
                size="small"
                :label-style="{ width: '64px' }"
              >
                <a-descriptions-item label="语言">{{
                  item.lang
                }}</a-descriptions-item>
                <a-descriptions-item label="框架">{{
                  item.framework
                }}</a-descriptions-item>
                <a-descriptions-item label="数据库">{{
                  item.db
                }}</a-descriptions-item>
                <a-descriptions-item label="仓库">
                  <a-typography-paragraph copyable :style="{ margin: 0 }">{{
                    item.repo
                  }}</a-typography-paragraph>
                </a-descriptions-item>
              </a-descriptions>
              <a-divider :style="{ margin: '10px 0' }" />
              <div class="awards">
                <a-statistic
                  title="规则数"
                  :value="item.rules"
                  :value-style="{ fontSize: '14px' }"
                />
                <a-statistic
                  title="预设数"
                  :value="item.preset"
                  :value-style="{ fontSize: '14px' }"
                />
                <a-statistic
                  title="使用量"
                  :value="item.usage"
                  :value-style="{ fontSize: '14px' }"
                />
                <a-statistic
                  title="更新(天)"
                  :value="item.updatedDays"
                  :value-style="{ fontSize: '14px' }"
                />
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';

  type Filters = {
    lang?: string;
    framework?: string;
    db?: string;
    scene?: string;
  };
  const filters = ref<Filters>({});
  const langOptions = ['Java', 'TypeScript', 'Python', 'Go'];
  const frameworkOptions = ['Spring Boot', 'NestJS', 'Django', 'Gin'];
  const dbOptions = ['MySQL', 'PostgreSQL', 'MongoDB'];
  const sceneOptions = ['CRUD', '微服务', '电商', '内容管理'];

  const onSelect = (k: keyof Filters, v: string) => {
    filters.value[k] = v;
  };
  const resetFilters = () => {
    filters.value = {};
  };
  const activeCount = computed(() => Object.keys(filters.value).length);

  // mock 20 items - 模板视角，贴合你的项目定位
  type Item = {
    id: number;
    title: string;
    lang: string;
    framework: string;
    db: string;
    scene: string;
    repo: string;
    cover: string;
    rules: number;
    preset: number;
    usage: number;
    updatedDays: number;
  };

  const mk = (i: number): Item => {
    const lang = langOptions[i % langOptions.length];
    const framework = frameworkOptions[i % frameworkOptions.length];
    const db = dbOptions[i % dbOptions.length];
    const scene = sceneOptions[i % sceneOptions.length];
    return {
      id: i + 1,
      title: `${framework} ${scene} 模板`,
      lang,
      framework,
      db,
      scene,
      repo: `github.com/example/${framework
        .toLowerCase()
        .replace(/\s+/g, '-')}-${scene}`,
      cover: `https://picsum.photos/seed/catalog-${i + 1}/800/450`,
      rules: Math.floor(Math.random() * 40) + 20,
      preset: Math.floor(Math.random() * 6) + 1,
      usage: Math.floor(Math.random() * 900) + 100,
      updatedDays: Math.floor(Math.random() * 60) + 1,
    };
  };

  const items = ref<Item[]>(Array.from({ length: 20 }).map((_, i) => mk(i)));

  const filteredList = computed(() => {
    return items.value.filter((it) => {
      const { lang, framework, db, scene } = filters.value;
      return (
        (!lang || it.lang === lang) &&
        (!framework || it.framework === framework) &&
        (!db || it.db === db) &&
        (!scene || it.scene === scene)
      );
    });
  });
</script>

<style scoped>
  .filter-bar {
    display: grid;
    grid-template-columns: 1fr auto;
    align-items: center;
    gap: 12px;
    background: #40404010;
    padding: 12px;
    border-radius: 12px;
    margin: 8px 0 20px;
  }
  .filter-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .cards {
    margin-top: 8px;
  }
  .card-body {
    padding: 8px 4px;
  }
  .card-title {
    display: flex;
    align-items: center;
  }
  .name {
    font-weight: 600;
  }
  .sub {
    color: var(--color-text-3);
    font-size: 12px;
  }
  .awards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 6px;
  }
</style>
