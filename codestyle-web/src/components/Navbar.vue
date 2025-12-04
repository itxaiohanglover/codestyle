<script setup lang="ts">
// import { ChevronDown } from 'lucide-vue-next'
</script>

<template>
  <nav class="sticky top-0 z-50 bg-background/80 backdrop-blur-md border-b border-gray-200">
    <div class="max-w-7xl mx-auto px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <div class="flex items-center space-x-2">
          <img src="/assets/image/codestyle-logo.png" alt="CodeStyle Logo" class="w-8 h-8" />
          <span class="text-xl font-bold text-gray-900">CodeStyle</span>
          <span class="text-sm text-gray-500 font-normal">码蜂</span>
        </div>

        <!-- Navigation Links -->
        <div class="hidden md:flex items-center space-x-8">
          <a href="#features" class="text-gray-600 hover:text-gray-900 transition-colors text-sm font-medium">
            产品特性
          </a>
          <a href="#use-cases" class="text-gray-600 hover:text-gray-900 transition-colors text-sm font-medium">
            适用场景
          </a>
          <a href="#tech-logic" class="text-gray-600 hover:text-gray-900 transition-colors text-sm font-medium">
            技术逻辑
          </a>
          <a href="#how-it-works" class="text-gray-600 hover:text-gray-900 transition-colors text-sm font-medium">
            工作流程
          </a>
          <a href="#faq" class="text-gray-600 hover:text-gray-900 transition-colors text-sm font-medium">
            常见问题
          </a>
        </div>

        <!-- User Button -->
        <div class="flex items-center">
          <a href="#cta" class="bg-primary-blue text-white px-6 py-2.5 rounded-lg hover:bg-blue-700 transition-colors font-bold text-sm shadow-lg hover:shadow-xl">
            立即开始
          </a>
        </div>
      </div>
    </div>
  </nav>
</template>
