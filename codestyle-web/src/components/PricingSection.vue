<script setup lang="ts">
import { Check } from 'lucide-vue-next'

const plans = [
  {
    name: '个人开发者',
    price: '免费',
    period: '永久免费',
    description: '沉淀个人风格',
    features: [
      'AI 生成代码直接用',
      '开发效率提升 50%',
      '最多 3 个风格模板',
      '社区支持',
    ],
    buttonText: '立即开始',
    buttonClass: 'bg-white text-gray-700 border-2 border-gray-300 hover:border-gray-400',
    highlighted: false,
  },
  {
    name: '中小型团队',
    price: '¥999',
    period: '每月',
    description: '统一代码风格',
    features: [
      '减少协作冲突',
      '新人 1 天上手',
      '无限风格模板',
      '最多 20 人协作',
      '优先技术支持',
    ],
    buttonText: '联系销售',
    buttonClass: 'bg-primary-blue text-white hover:bg-blue-700',
    highlighted: false,
  },
  {
    name: '大型企业',
    price: '定制',
    period: '按需定价',
    description: '构建跨部门代码资产库',
    features: [
      '传承技术智慧',
      '降本增效',
      '私有化部署',
      '无限团队成员',
      '定制化开发',
      '企业级 SLA 保障',
    ],
    buttonText: '联系我们',
    buttonClass: 'bg-primary-purple text-white hover:bg-purple-700',
    highlighted: true,
    badge: '推荐',
  },
]
</script>

<template>
  <section id="pricing" class="py-20 px-6 lg:px-8 bg-white">
    <div class="max-w-7xl mx-auto">
      <!-- Section Header -->
      <div class="text-center mb-16">
        <div class="inline-block bg-purple-50 text-primary-purple px-4 py-1.5 rounded-full text-sm font-semibold mb-4">
          定价方案
        </div>
        <h2 class="text-4xl md:text-5xl font-bold text-gray-900">
          选择适合你的方案
        </h2>
      </div>

      <!-- Pricing Cards -->
      <div class="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
        <div 
          v-for="plan in plans" 
          :key="plan.name"
          class="relative bg-white rounded-2xl p-8 border-2 transition-all duration-300 hover-lift cursor-pointer"
          :class="plan.highlighted 
            ? 'border-primary-purple shadow-2xl shadow-purple-200 scale-105 hover:scale-110' 
            : 'border-gray-200 shadow-lg hover:shadow-2xl hover:border-primary-blue'"
        >
          <!-- Best Value Badge -->
          <div 
            v-if="plan.badge"
            class="absolute -top-4 left-1/2 transform -translate-x-1/2"
          >
            <span class="bg-primary-purple text-white px-6 py-1.5 rounded-full text-sm font-bold shadow-lg">
              {{ plan.badge }}
            </span>
          </div>

          <!-- Plan Header -->
          <div class="text-center mb-8">
            <h3 class="text-2xl font-bold text-gray-900 mb-2">{{ plan.name }}</h3>
            <div class="flex items-baseline justify-center mb-2">
              <span class="text-5xl font-bold text-gray-900">{{ plan.price }}</span>
              <span class="text-gray-600 ml-2">{{ plan.period }}</span>
            </div>
            <p class="text-gray-600 text-sm">{{ plan.description }}</p>
          </div>

          <!-- CTA Button -->
          <button 
            class="w-full py-4 rounded-lg font-semibold text-base transition-all shadow-md hover:shadow-xl mb-8 shine relative overflow-hidden group"
            :class="plan.buttonClass"
          >
            <span class="relative z-10">{{ plan.buttonText }}</span>
          </button>

          <!-- Features List -->
          <ul class="space-y-4">
            <li 
              v-for="feature in plan.features" 
              :key="feature"
              class="flex items-start space-x-3"
            >
              <Check class="w-5 h-5 text-green-500 flex-shrink-0 mt-0.5" :stroke-width="3" />
              <span class="text-gray-700 text-sm">{{ feature }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>
