<script setup lang="ts">
import { FileText, Layers, Zap, Wrench, CheckCircle } from 'lucide-vue-next'

const features = [
  { icon: FileText, label: '代码资产管理' },
  { icon: Layers, label: 'RAG 检索' },
  { icon: Zap, label: 'MCP 集成' },
  { icon: Wrench, label: '多租户隔离' },
  { icon: CheckCircle, label: '权限控制' },
]
</script>

<template>
  <section id="features" class="py-20 px-6 lg:px-8 bg-white">
    <div class="max-w-6xl mx-auto">
      <!-- Code Badge -->
      <div class="text-center mb-6">

      </div>

      <!-- Section Title -->
      <h2 class="text-4xl md:text-5xl font-bold text-center text-gray-900 mb-4">
        官网在线制模 + <br />轻量化插件检索
      </h2>
      <p class="text-xl text-center text-gray-600 mb-12 max-w-3xl mx-auto">
        <span class="font-semibold text-primary-blue">官网端</span>支持在线制作风格模板（CRUD、接口规范等），<span class="font-semibold text-primary-purple">本地 MCP 插件</span>仅负责检索+调用，让 AI 精准输出贴合你风格的代码。
      </p>

      <!-- Feature Icons -->
      <div class="flex flex-wrap items-center justify-center gap-8 mb-16">
        <div 
          v-for="(feature, index) in features" 
          :key="index"
          class="flex flex-col items-center space-y-3 hover-lift cursor-pointer group"
        >
          <div class="bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl p-4 shadow-lg group-hover:shadow-2xl group-hover:scale-110 transition-all duration-300">
            <component :is="feature.icon" class="w-8 h-8 text-white" :stroke-width="2" />
          </div>
          <span class="text-sm font-semibold text-gray-700 text-center max-w-[100px] group-hover:text-primary-blue transition-colors">
            {{ feature.label }}
          </span>
        </div>
      </div>

      <!-- Tech Stack Details -->
      <div class="bg-gray-50 rounded-2xl p-8 border border-gray-200 mb-16">
        <h3 class="text-2xl font-bold text-gray-900 mb-6">核心技术优势</h3>
        <div class="grid md:grid-cols-2 gap-6">
          <div class="flex items-start space-x-3">
            <CheckCircle class="w-6 h-6 text-green-500 flex-shrink-0 mt-0.5" />
            <div>
              <p class="text-gray-700 font-medium">插件轻量化</p>
              <p class="text-sm text-gray-600">摒弃复杂文件阅读与算法，仅做检索+调用，安装快、不占资源</p>
            </div>
          </div>
          <div class="flex items-start space-x-3">
            <CheckCircle class="w-6 h-6 text-green-500 flex-shrink-0 mt-0.5" />
            <div>
              <p class="text-gray-700 font-medium">模板管理灵活</p>
              <p class="text-sm text-gray-600">官网在线可视化制模，支持 CRUD 等场景化模板，更新同步即时生效</p>
            </div>
          </div>
          <div class="flex items-start space-x-3">
            <CheckCircle class="w-6 h-6 text-green-500 flex-shrink-0 mt-0.5" />
            <div>
              <p class="text-gray-700 font-medium">本地组装安全</p>
              <p class="text-sm text-gray-600">模板与本地上下文在本地组装，避免代码隐私泄露风险</p>
            </div>
          </div>
          <div class="flex items-start space-x-3">
            <CheckCircle class="w-6 h-6 text-green-500 flex-shrink-0 mt-0.5" />
            <div>
              <p class="text-gray-700 font-medium">零成本接入</p>
              <p class="text-sm text-gray-600">3 分钟完成"官网注册-插件安装-IDE 对接"全流程</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Effect Comparison -->
      <div class="mt-16">
        <h3 class="text-3xl font-bold text-center text-gray-900 mb-4">效果对比</h3>
        <p class="text-center text-gray-600 mb-8">
          <span class="font-semibold">示例需求：</span>生成学生管理 Controller 代码
        </p>

        <div class="grid md:grid-cols-2 gap-8">
          <!-- 对比图1 - 未使用CodeStyle -->
          <div class="bg-white rounded-2xl border-2 border-gray-200 overflow-hidden shadow-lg flex flex-col">
            <div class="bg-red-50 border-b border-red-200 px-6 py-3">
              <h4 class="font-semibold text-red-700 flex items-center">
                <span class="w-2 h-2 bg-red-500 rounded-full mr-2"></span>
                未使用 CodeStyle
              </h4>
            </div>
            <div class="p-4 flex-1 flex items-center justify-center">
              <img 
                src="/assets/image/eg1.png" 
                alt="未使用CodeStyle的代码示例" 
                class="w-full h-full object-contain rounded-lg"
              />
            </div>
          </div>

          <!-- 对比图2 - 使用CodeStyle -->
          <div class="bg-white rounded-2xl border-2 border-green-200 overflow-hidden shadow-lg flex flex-col">
            <div class="bg-green-50 border-b border-green-200 px-6 py-3">
              <h4 class="font-semibold text-green-700 flex items-center">
                <span class="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                使用 CodeStyle
              </h4>
            </div>
            <div class="p-4 flex-1 flex items-center justify-center">
              <img 
                src="/assets/image/eg2.png" 
                alt="使用CodeStyle的代码示例" 
                class="w-full h-full object-contain rounded-lg"
              />
            </div>
          </div>
        </div>

        <!-- 效果说明 -->
        <div class="mt-8 bg-gradient-to-r from-blue-50 to-purple-50 rounded-2xl p-6 border border-blue-200">
          <p class="text-center text-gray-700 text-lg">
            <span class="font-bold text-primary-blue">风格统一后</span>，既能
            <span class="font-semibold text-green-600">提升代码风格和设计</span>，还能
            <span class="font-semibold text-purple-600">减少 Token 的输出</span>。
          </p>
        </div>
      </div>

      <!-- Core Value Section -->
      <div class="mt-20">
        <h3 class="text-3xl font-bold text-center text-gray-900 mb-12">核心价值：效率+质量双提升</h3>
        <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-2xl p-6 border-2 border-blue-200 hover-lift">
            <div class="text-4xl font-bold text-primary-blue mb-2">90%</div>
            <p class="text-gray-700 font-semibold mb-1">AI 风格对齐</p>
            <p class="text-sm text-gray-600">生成代码贴合度提升 90%，减少修改时间</p>
          </div>
          <div class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-2xl p-6 border-2 border-purple-200 hover-lift">
            <div class="text-4xl font-bold text-primary-purple mb-2">60%+</div>
            <p class="text-gray-700 font-semibold mb-1">资产利用率翻倍</p>
            <p class="text-sm text-gray-600">历史代码复用率从 10% 提至 60%+</p>
          </div>
          <div class="bg-gradient-to-br from-green-50 to-green-100 rounded-2xl p-6 border-2 border-green-200 hover-lift">
            <div class="text-4xl font-bold text-green-600 mb-2">80%</div>
            <p class="text-gray-700 font-semibold mb-1">新人上手提速</p>
            <p class="text-sm text-gray-600">无需啃旧项目，快速融入团队</p>
          </div>
          <div class="bg-gradient-to-br from-orange-50 to-orange-100 rounded-2xl p-6 border-2 border-orange-200 hover-lift">
            <div class="text-4xl font-bold text-orange-600 mb-2">3分钟</div>
            <p class="text-gray-700 font-semibold mb-1">零成本接入</p>
            <p class="text-sm text-gray-600">不改造现有流程，即插即用</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
