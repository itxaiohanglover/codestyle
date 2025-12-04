<script setup lang="ts">
import { User, Users, Building2 } from 'lucide-vue-next'

const useCases = [
  {
    icon: 'User',
    title: '个人开发者',
    description: '沉淀个人风格，AI生成代码直接用',
    benefit: '开发效率提升 50%',
    color: 'from-blue-500 to-cyan-500',
    bgColor: 'bg-blue-50',
  },
  {
    icon: 'Users',
    title: '中小型团队',
    description: '统一代码风格，减少协作冲突',
    benefit: '新人 1 天上手',
    color: 'from-purple-500 to-pink-500',
    bgColor: 'bg-purple-50',
  },
  {
    icon: 'Building2',
    title: '大型企业',
    description: '构建跨部门代码资产库，传承技术智慧',
    benefit: '降本增效',
    color: 'from-orange-500 to-red-500',
    bgColor: 'bg-orange-50',
  },
]
</script>

<template>
  <section id="use-cases" class="py-20 px-6 lg:px-8 bg-gradient-to-b from-white to-blue-50">
    <div class="max-w-6xl mx-auto">
      <!-- Section Title -->
      <h2 class="text-4xl md:text-5xl font-bold text-center text-gray-900 mb-4">
        谁在用？全场景覆盖
      </h2>
      <p class="text-xl text-center text-gray-600 mb-16">
        从个人到企业，CodeStyle 赋能每一个开发场景
      </p>

      <!-- Use Cases Grid -->
      <div class="grid md:grid-cols-3 gap-8">
        <div 
          v-for="(useCase, index) in useCases" 
          :key="index"
          class="group"
        >
          <div class="bg-white rounded-3xl shadow-lg border-2 border-gray-200 overflow-hidden hover-lift hover:shadow-2xl transition-all duration-300 h-full">
            <!-- Icon Header -->
            <div :class="['p-8 flex justify-center', useCase.bgColor]">
              <div :class="['w-20 h-20 rounded-2xl bg-gradient-to-br flex items-center justify-center group-hover:scale-110 transition-transform duration-300', useCase.color]">
                <component :is="useCase.icon === 'User' ? User : useCase.icon === 'Users' ? Users : Building2" :size="40" :stroke-width="2" class="text-white" />
              </div>
            </div>

            <!-- Content -->
            <div class="p-8">
              <h3 class="text-2xl font-bold text-gray-900 mb-4 text-center">
                {{ useCase.title }}
              </h3>
              <p class="text-gray-600 leading-relaxed mb-6 text-center">
                {{ useCase.description }}
              </p>

              <!-- Benefit Badge -->
              <div :class="['text-center py-3 px-6 rounded-xl bg-gradient-to-r font-bold text-white text-lg shadow-lg', useCase.color]">
                {{ useCase.benefit }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
