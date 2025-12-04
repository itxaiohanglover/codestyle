<script setup lang="ts">
const footerLinks = {
  links: ['产品支持', '定价方案', '文档中心', 'API 文档'],
  legal: ['服务条款', '隐私政策', 'Cookie 政策', '开源许可'],
}
</script>

<template>
  <footer class="bg-background-footer text-gray-300 py-16 px-6 lg:px-8">
    <div class="max-w-7xl mx-auto">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
        <!-- Column 1: Brand -->
        <div class="space-y-4">
          <div class="flex items-center space-x-2">
            <img src="/assets/image/codestyle-logo.png" alt="CodeStyle Logo" class="w-8 h-8" />
            <span class="text-xl font-bold text-white">CodeStyle</span>
            <span class="text-sm text-gray-400">码蜂</span>
          </div>
          <p class="text-sm text-gray-400 leading-relaxed">
            基于 RAG 技术的企业级代码知识库，让团队私有代码资产生成高质量代码。
          </p>
          <p class="text-xs text-gray-500">
            © 2024-2025 CodeStyle. 保留所有权利。
          </p>
        </div>

        <!-- Column 2: Links -->
        <div>
          <h3 class="text-white font-semibold mb-4 uppercase text-sm tracking-wider">快速链接</h3>
          <ul class="space-y-3">
            <li v-for="link in footerLinks.links" :key="link">
              <a href="#" class="text-gray-400 hover:text-white transition-colors text-sm">
                {{ link }}
              </a>
            </li>
          </ul>
        </div>

        <!-- Column 3: Legal -->
        <div>
          <h3 class="text-white font-semibold mb-4 uppercase text-sm tracking-wider">法律信息</h3>
          <ul class="space-y-3">
            <li v-for="link in footerLinks.legal" :key="link">
              <a href="#" class="text-gray-400 hover:text-white transition-colors text-sm">
                {{ link }}
              </a>
            </li>
          </ul>
        </div>

        <!-- Column 4: Feedback -->
        <div>
          <h3 class="text-white font-semibold mb-4 uppercase text-sm tracking-wider">加入社区</h3>
          <a 
            href="#" 
            class="inline-flex items-center space-x-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg transition-colors text-sm font-medium"
          >
            <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
              <path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515a.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0a12.64 12.64 0 0 0-.617-1.25a.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057a19.9 19.9 0 0 0 5.993 3.03a.078.078 0 0 0 .084-.028a14.09 14.09 0 0 0 1.226-1.994a.076.076 0 0 0-.041-.106a13.107 13.107 0 0 1-1.872-.892a.077.077 0 0 1-.008-.128a10.2 10.2 0 0 0 .372-.292a.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127a12.299 12.299 0 0 1-1.873.892a.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028a19.839 19.839 0 0 0 6.002-3.03a.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419c0-1.333.956-2.419 2.157-2.419c1.21 0 2.176 1.096 2.157 2.42c0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419c0-1.333.955-2.419 2.157-2.419c1.21 0 2.176 1.096 2.157 2.42c0 1.333-.946 2.418-2.157 2.418z"/>
            </svg>
            <span>Discord</span>
          </a>
        </div>
      </div>

      <!-- Bottom Border -->
      <div class="border-t border-gray-700 pt-8">
        <p class="text-center text-sm text-gray-500">
          用代码资产驱动 AI 开发 🐝
        </p>
      </div>
    </div>
  </footer>
</template>
