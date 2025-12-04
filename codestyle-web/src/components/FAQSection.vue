<script setup lang="ts">
import { ref } from 'vue'
import { Plus, Minus } from 'lucide-vue-next'

interface FAQItem {
  question: string
  answer: string
  isOpen: boolean
}

const faqs = ref<FAQItem[]>([
  {
    question: '码蜂和普通的代码规范工具有什么区别？',
    answer: '码蜂不是普通的代码规范工具，而是"官网在线制模+轻量化插件检索"的企业级代码风格复用平台。官网端支持在线制作风格模板，本地 MCP 插件仅负责检索+调用，让 AI 精准输出贴合你风格的代码。',
    isOpen: false,
  },
  {
    question: '插件会占用很多资源吗？',
    answer: '不会。我们采用轻量化设计，插件摒弃复杂文件阅读与算法，仅做检索+调用，安装快、不占资源。所有模板管理和优化都在官网端完成。',
    isOpen: false,
  },
  {
    question: '我的代码会被上传到云端吗？',
    answer: '不会。模板与本地上下文在本地组装，避免代码隐私泄露风险。只有你主动制作的风格模板会存储在官网，你的实际业务代码始终保留在本地。',
    isOpen: false,
  },
  {
    question: '接入需要多长时间？',
    answer: '只需 3 分钟即可完成"官网注册-插件安装-IDE 对接"全流程，零成本接入，不改造现有开发流程。',
    isOpen: false,
  },
  {
    question: '支持哪些场景的模板？',
    answer: '支持 CRUD、接口规范、组件模板、微服务脚手架等各类场景化模板。你可以通过可视化界面制作，也可以直接导入历史优质代码自动生成模板。',
    isOpen: false,
  },
])

const toggleFAQ = (index: number) => {
  faqs.value[index].isOpen = !faqs.value[index].isOpen
}
</script>

<template>
  <section id="faq" class="py-20 px-6 lg:px-8 bg-background">
    <div class="max-w-6xl mx-auto">
      <div class="grid md:grid-cols-2 gap-12">
        <!-- Left Column: Title -->
        <div>
          <div class="inline-block bg-purple-50 text-primary-purple px-4 py-1.5 rounded-full text-sm font-semibold mb-4">
            常见问题
          </div>
          <h2 class="text-4xl md:text-5xl font-bold text-gray-900 leading-tight">
            您想了解的<br />问题
          </h2>
        </div>

        <!-- Right Column: Accordion -->
        <div class="space-y-4">
          <div 
            v-for="(faq, index) in faqs" 
            :key="index"
            class="bg-white rounded-xl border border-gray-200 overflow-hidden transition-all hover:shadow-lg hover:border-primary-blue"
          >
            <button
              @click="toggleFAQ(index)"
              class="w-full flex items-center justify-between p-6 text-left hover:bg-gray-50 transition-colors group"
            >
              <span class="font-semibold text-gray-900 pr-4 group-hover:text-primary-blue transition-colors">{{ faq.question }}</span>
              <Plus 
                v-if="!faq.isOpen"
                class="w-5 h-5 text-gray-400 flex-shrink-0 group-hover:text-primary-blue group-hover:rotate-90 transition-all duration-300" 
                :stroke-width="2.5"
              />
              <Minus 
                v-else
                class="w-5 h-5 text-primary-purple flex-shrink-0" 
                :stroke-width="2.5"
              />
            </button>
            <div 
              v-if="faq.isOpen"
              class="px-6 pb-6 text-gray-600 leading-relaxed fade-in"
            >
              {{ faq.answer }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
