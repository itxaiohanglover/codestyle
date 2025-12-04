<script setup lang="ts">
import { ArrowRight } from 'lucide-vue-next'

const steps = [
  {
    label: '输入',
    title: '开发需求 + 本地上下文',
    description: '如"生成符合团队风格的用户 CRUD 接口"',
    color: 'from-blue-500 to-cyan-500',
    bgColor: 'bg-blue-50',
  },
  {
    label: '匹配',
    title: '智能检索 + 模板匹配',
    description: '插件检索上下文后，请求官网匹配最优风格模板',
    color: 'from-purple-500 to-pink-500',
    bgColor: 'bg-purple-50',
  },
  {
    label: '输出',
    title: '本地组装 + 代码生成',
    description: '模板与本地上下文在本地组装，生成贴合风格的代码，全程高效且安全',
    color: 'from-green-500 to-emerald-500',
    bgColor: 'bg-green-50',
  },
]
</script>

<template>
  <section id="tech-logic" class="py-20 px-6 lg:px-8 bg-white">
    <div class="max-w-6xl mx-auto">
      <!-- Section Title -->
      <h2 class="text-4xl md:text-5xl font-bold text-center text-gray-900 mb-4">
        核心技术逻辑：输入-匹配-输出
      </h2>
      <p class="text-xl text-center text-gray-600 mb-16">
        三步闭环，精准匹配你的代码风格
      </p>

      <!-- Steps Flow -->
      <div class="grid md:grid-cols-3 gap-6">
        <div 
          v-for="(step, index) in steps" 
          :key="index"
          class="relative"
        >
          <!-- Card -->
          <div class="bg-white rounded-3xl shadow-lg border-2 border-gray-200 overflow-hidden hover-lift hover:shadow-2xl transition-all duration-300 h-full">
            <!-- Header with Label -->
            <div :class="['p-6 text-center', step.bgColor]">
              <div :class="['inline-block px-6 py-2 rounded-full bg-gradient-to-r text-white font-bold text-lg shadow-lg', step.color]">
                {{ step.label }}
              </div>
            </div>

            <!-- Content -->
            <div class="p-8">
              <h3 class="text-xl font-bold text-gray-900 mb-4 text-center">
                {{ step.title }}
              </h3>
              <p class="text-gray-600 leading-relaxed text-center">
                {{ step.description }}
              </p>
            </div>
          </div>

          <!-- Arrow between cards (except last) -->
          <div v-if="index < steps.length - 1" class="hidden md:flex absolute top-1/2 -right-8 transform -translate-y-1/2 z-10">
            <ArrowRight :size="48" :stroke-width="2" class="text-primary-blue animate-pulse" />
          </div>
        </div>
      </div>

      <!-- Bottom Summary -->
      <div class="mt-16 bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl p-8 text-center text-white shadow-2xl">
        <p class="text-xl font-semibold">
           精准匹配 ·  本地组装 ·  高效安全
        </p>
      </div>
    </div>
  </section>
</template>
