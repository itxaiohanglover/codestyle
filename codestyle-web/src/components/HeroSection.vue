<script setup lang="ts">
import { Zap, ArrowRight, Star } from 'lucide-vue-next'
</script>

<template>
  <section class="py-20 px-6 lg:px-8">
    <div class="max-w-4xl mx-auto text-center">
      <!-- Badge -->
      <div class="inline-flex items-center space-x-2 bg-blue-50 border border-blue-200 rounded-full px-4 py-2 mb-8 shine hover-lift">
        <div class="bg-primary-blue rounded p-1 pulse-slow">
          <Zap class="w-3 h-3 text-white" :stroke-width="2.5" />
        </div>
        <span class="text-sm font-medium text-blue-700">企业级代码知识库 · 即插即用</span>
      </div>

      <!-- Main Heading -->
      <h1 class="text-5xl md:text-6xl lg:text-7xl font-bold text-gray-900 leading-tight mb-6 fade-in">
        让历史代码活起来，<br />
        <span class="gradient-text">让AI写的更对味！</span>
      </h1>

      <!-- Subheading -->
      <div class="mb-10 max-w-3xl mx-auto">
        <p class="text-lg text-gray-600 mb-4 leading-relaxed">
          AI 能写代码，却总写不对"你的风格"？历史优质代码躺平不用？新人上手慢？
        </p>
        <p class="text-xl font-bold text-gray-900">
          码蜂一次性解决三大开发顽疾，让 AI 精准复用你和团队的代码基因
        </p>
      </div>

      <!-- CTA Buttons -->
      <div class="flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-4 mb-16">
        <button class="bg-primary-blue text-white px-8 py-4 rounded-lg font-semibold text-base hover:bg-blue-700 transition-all shadow-lg hover:shadow-xl flex items-center space-x-2 shine group">
          <span>立即体验</span>
          <ArrowRight class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
        </button>
        <!-- <button class="bg-white text-gray-700 px-8 py-4 rounded-lg font-semibold text-base border-2 border-gray-300 hover:border-primary-blue hover:text-primary-blue transition-all hover-lift">
          查看定价方案
        </button> -->
      </div>

      <!-- Social Proof -->
      <div class="flex items-center justify-center space-x-4">
        <div class="flex -space-x-2">
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-purple-400 to-pink-400 border-2 border-white"></div>
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-cyan-400 border-2 border-white"></div>
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-green-400 to-emerald-400 border-2 border-white"></div>
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-orange-400 to-red-400 border-2 border-white"></div>
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-yellow-400 to-amber-400 border-2 border-white"></div>
        </div>
        <div class="flex items-center space-x-2">
          <div class="flex items-center">
            <Star class="w-4 h-4 text-yellow-400 fill-yellow-400" />
            <Star class="w-4 h-4 text-yellow-400 fill-yellow-400" />
            <Star class="w-4 h-4 text-yellow-400 fill-yellow-400" />
            <Star class="w-4 h-4 text-yellow-400 fill-yellow-400" />
            <Star class="w-4 h-4 text-yellow-400 fill-yellow-400" />
          </div>
          <span class="text-sm text-gray-600 font-medium">已助力 50+ 企业团队提效</span>
        </div>
      </div>
    </div>
  </section>
</template>
