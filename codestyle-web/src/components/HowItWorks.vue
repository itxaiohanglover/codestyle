<script setup lang="ts">
const steps = [
  {
    number: '01',
    title: '官网制模：定义专属风格',
    subtitle: '可视化界面，3分钟完成',
    description: '登录官网，通过可视化界面制作/上传风格模板（支持 CRUD、接口、组件等场景），或直接导入历史优质代码自动生成模板。模板由 Python 后端优化存储，支持分类管理。',
    features: ['可视化制模界面', '支持多场景模板', '历史代码自动生成', 'Python 后端优化'],
  },
  {
    number: '02',
    title: '插件部署：轻量接入本地',
    subtitle: '零配置，即插即用',
    description: '下载安装 MCP 插件，无需复杂配置，插件仅具备本地仓库检索能力（轻量化设计，无冗余功能），快速对接你的 AI IDE。',
    features: ['轻量化设计', '零配置部署', '仅做检索+调用', '秒级对接 IDE'],
  },
  {
    number: '03',
    title: '检索组装：精准调用省时间',
    subtitle: '本地组装，安全高效',
    description: '开发时 AI 触发需求，插件检索本地仓库上下文后，自动请求官网匹配风格模板，在本地组装模板与上下文信息，返回贴合场景的代码。模板更新后官网同步，插件自动拉取最新版本。',
    features: ['智能上下文检索', '自动匹配模板', '本地安全组装', '实时同步更新'],
  },
]
</script>

<template>
  <section id="how-it-works" class="py-20 px-6 lg:px-8 bg-background">
    <div class="max-w-6xl mx-auto">
      <!-- Section Title -->
      <h2 class="text-4xl md:text-5xl font-bold text-center text-gray-900 mb-4">
        3步实现：让 AI 懂你的代码
      </h2>
      <p class="text-xl text-center text-gray-600 mb-16">
        从制模到部署，再到精准调用，全程只需 3 步
      </p>

      <!-- Steps -->
      <div class="space-y-8">
        <div 
          v-for="(step, index) in steps" 
          :key="index"
          class="group"
        >
          <div class="bg-white rounded-3xl shadow-lg border-2 border-gray-200 overflow-hidden hover-lift hover:border-primary-blue transition-all duration-300">
            <div class="grid md:grid-cols-5 gap-0">
              <!-- Left: Number -->
              <div class="md:col-span-1 bg-gradient-to-br from-blue-50 to-purple-50 p-8 flex flex-col items-center justify-center border-r-2 border-gray-200">
                <div class="text-7xl font-black text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600 group-hover:scale-110 transition-transform duration-300">
                  {{ step.number }}
                </div>
              </div>

              <!-- Right: Content -->
              <div class="md:col-span-4 p-8">
                <div class="flex items-start justify-between mb-4">
                  <div>
                    <h3 class="text-2xl font-bold text-gray-900 mb-2 group-hover:text-primary-blue transition-colors">
                      {{ step.title }}
                    </h3>
                    <p class="text-sm text-primary-purple font-semibold">{{ step.subtitle }}</p>
                  </div>
                </div>
                
                <p class="text-gray-600 leading-relaxed mb-6">
                  {{ step.description }}
                </p>

                <!-- Features Grid -->
                <div class="grid grid-cols-2 gap-3">
                  <div 
                    v-for="(feature, idx) in step.features" 
                    :key="idx"
                    class="flex items-center space-x-2 bg-gray-50 rounded-lg px-3 py-2 group-hover:bg-blue-50 transition-colors"
                  >
                    <svg class="w-4 h-4 text-green-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path>
                    </svg>
                    <span class="text-sm text-gray-700 font-medium">{{ feature }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Connector Arrow (except last) -->
          <div v-if="index < steps.length - 1" class="flex justify-center my-6">
            <svg class="w-8 h-12 text-primary-blue animate-bounce" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v10.586l2.293-2.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L9 14.586V4a1 1 0 011-1z" clip-rule="evenodd"></path>
            </svg>
          </div>
        </div>
      </div>

      <!-- Bottom CTA -->
      <div class="mt-16 bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl p-8 text-center text-white shadow-2xl">
        <h4 class="text-2xl font-bold mb-3">准备好让 AI 理解你的代码风格了吗？</h4>
        <p class="text-lg opacity-90 mb-6">
          3 分钟完成接入，立即体验代码风格复用的强大能力
        </p>
        <button class="bg-white text-primary-blue px-8 py-4 rounded-lg font-bold text-lg hover:shadow-2xl hover:scale-105 transition-all shine">
          立即开始使用 →
        </button>
      </div>
    </div>
  </section>
</template>
