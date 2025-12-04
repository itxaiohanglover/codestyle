<script setup lang="ts">
import { Download, Rocket, Zap } from 'lucide-vue-next'

const steps = [
  {
    icon: 'Download',
    text: '点击下载',
    color: 'text-blue-500',
  },
  {
    icon: 'Zap',
    text: '3分钟接入AI IDE',
    color: 'text-purple-500',
  },
  {
    icon: 'Rocket',
    text: '即刻解锁代码风格复用能力',
    color: 'text-green-500',
  },
]
</script>

<template>
  <section id="cta" class="py-20 px-6 lg:px-8 bg-gradient-to-br from-blue-600 via-purple-600 to-pink-600 relative overflow-hidden">
    <!-- Background Pattern -->
    <div class="absolute inset-0 opacity-10">
      <div class="absolute top-0 left-0 w-96 h-96 bg-white rounded-full blur-3xl"></div>
      <div class="absolute bottom-0 right-0 w-96 h-96 bg-white rounded-full blur-3xl"></div>
    </div>

    <div class="max-w-5xl mx-auto relative z-10">
      <!-- Main Title -->
      <h2 class="text-4xl md:text-6xl font-black text-center text-white mb-6">
        立即体验：3步解锁专属代码风格
      </h2>
      <p class="text-xl md:text-2xl text-center text-white/90 mb-12 font-medium">
        官网注册制模 → 下载安装MCP插件 → 对接AI IDE即刻使用
      </p>

      <!-- Steps Flow -->
      <div class="flex flex-col md:flex-row items-center justify-center gap-6 mb-12">
        <div 
          v-for="(step, index) in steps" 
          :key="index"
          class="flex items-center"
        >
          <!-- Step Card -->
          <div class="bg-white/95 backdrop-blur-sm rounded-2xl shadow-2xl px-6 py-4 hover:scale-105 transition-transform duration-300">
            <div class="flex items-center space-x-3">
              <component :is="step.icon === 'Download' ? Download : step.icon === 'Zap' ? Zap : Rocket" :size="32" :stroke-width="2.5" :class="step.color" />
              <span class="text-gray-900 font-bold text-lg">{{ step.text }}</span>
            </div>
          </div>

          <!-- Arrow (except last) -->
          <div v-if="index < steps.length - 1" class="hidden md:block mx-4">
            <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd"></path>
            </svg>
          </div>
        </div>
      </div>

      <!-- CTA Buttons -->
      <div class="flex flex-col md:flex-row items-center justify-center gap-6">
        <button class="bg-white text-primary-blue px-10 py-5 rounded-2xl font-black text-xl hover:shadow-2xl hover:scale-110 transition-all shine flex items-center space-x-3 group">
          <Download :size="28" :stroke-width="2.5" class="group-hover:animate-bounce" />
          <span>立即下载插件</span>
        </button>
        <button class="bg-transparent border-4 border-white text-white px-10 py-5 rounded-2xl font-black text-xl hover:bg-white hover:text-primary-blue hover:shadow-2xl hover:scale-110 transition-all">
          官网注册制模
        </button>
      </div>

      <!-- Bottom Note -->
      <p class="text-center text-white/80 text-lg mt-8">
        ✨ 无需信用卡 · 💯 免费试用 · ⚡ 秒级接入
      </p>
    </div>
  </section>
</template>
