<script setup lang="ts">
</script>

<template>
  <section class="py-20 px-6 lg:px-8 bg-background">
    <div class="max-w-7xl mx-auto">
      <!-- Section Title -->
      <h2 class="text-4xl md:text-5xl font-bold text-center text-gray-900 mb-4">
        直击三大核心痛点
      </h2>
      <p class="text-xl text-center text-gray-600 mb-16 max-w-3xl mx-auto">
        AI 生成"对而不适"、历史代码"躺平闲置"、新人上手"熬秃头皮"
      </p>

      <!-- Problem Cards with Scenarios -->
      <div class="grid md:grid-cols-3 gap-8 mb-12">
        <!-- Pain Point 1 -->
        <div class="bg-white rounded-2xl shadow-lg border-2 border-red-200 overflow-hidden hover-lift group">
          <div class="bg-gradient-to-r from-red-50 to-orange-50 p-6 border-b-2 border-red-200">
            <div class="text-5xl mb-3 text-center">😤</div>
            <h3 class="text-xl font-bold text-gray-900 text-center mb-2">AI 生成"对而不适"</h3>
            <p class="text-center text-sm text-red-600 font-semibold">改到崩溃</p>
          </div>
          <div class="p-6">
            <div class="mb-4">
              <div class="flex items-start mb-2">
                <span class="inline-block bg-red-100 text-red-700 text-xs font-semibold px-2 py-1 rounded mr-2 flex-shrink-0">场景</span>
              </div>
              <p class="text-sm text-gray-600 leading-relaxed">
                用 AI 生成 CRUD 接口，语法没错，但团队 CRUD 遵循"查询用 getXXX+分页封装固定参数"的风格，AI 却生成了杂乱的方法名和参数结构；前端组件团队习惯的表单校验模板，AI 完全没匹配上。
              </p>
            </div>
            <div class="border-t border-gray-200 pt-4">
              <div class="flex items-start mb-2">
                <span class="inline-block bg-green-100 text-green-700 text-xs font-semibold px-2 py-1 rounded mr-2 flex-shrink-0">解决</span>
              </div>
              <p class="text-sm text-gray-700 leading-relaxed">
                官网提前制作/上传团队 CRUD 等专属风格模板，插件检索本地场景后调用模板，<span class="font-semibold text-primary-blue">AI 生成的代码直接贴合团队固定写法，无需修改</span>。
              </p>
            </div>
          </div>
        </div>

        <!-- Pain Point 2 -->
        <div class="bg-white rounded-2xl shadow-lg border-2 border-orange-200 overflow-hidden hover-lift group">
          <div class="bg-gradient-to-r from-orange-50 to-yellow-50 p-6 border-b-2 border-orange-200">
            <div class="text-5xl mb-3 text-center">😓</div>
            <h3 class="text-xl font-bold text-gray-900 text-center mb-2">历史代码"躺平闲置"</h3>
            <p class="text-center text-sm text-orange-600 font-semibold">重复造轮子</p>
          </div>
          <div class="p-6">
            <div class="mb-4">
              <div class="flex items-start mb-2">
                <span class="inline-block bg-orange-100 text-orange-700 text-xs font-semibold px-2 py-1 rounded mr-2 flex-shrink-0">场景</span>
              </div>
              <p class="text-sm text-gray-600 leading-relaxed">
                3 年前做过的支付对接模块稳定运行，但新需求时找不到文件；数据校验工具明明有现成的，新人却重新开发踩坑 3 天。
              </p>
            </div>
            <div class="border-t border-gray-200 pt-4">
              <div class="flex items-start mb-2">
                <span class="inline-block bg-green-100 text-green-700 text-xs font-semibold px-2 py-1 rounded mr-2 flex-shrink-0">解决</span>
              </div>
              <p class="text-sm text-gray-700 leading-relaxed">
                结构化沉淀所有优质资产，附带场景化使用文档，AI 按需调用，实现<span class="font-semibold text-primary-purple">"一次开发，终身复用"</span>，彻底告别重复劳动。
              </p>
            </div>
          </div>
        </div>

        <!-- Pain Point 3 -->
        <div class="bg-white rounded-2xl shadow-lg border-2 border-purple-200 overflow-hidden hover-lift group">
          <div class="bg-gradient-to-r from-purple-50 to-pink-50 p-6 border-b-2 border-purple-200">
            <div class="text-5xl mb-3 text-center">😵</div>
            <h3 class="text-xl font-bold text-gray-900 text-center mb-2">新人"熬秃头皮"</h3>
            <p class="text-center text-sm text-purple-600 font-semibold">适配成本高</p>
          </div>
          <div class="p-6">
            <div class="mb-4">
              <div class="flex items-start mb-2">
                <span class="inline-block bg-purple-100 text-purple-700 text-xs font-semibold px-2 py-1 rounded mr-2 flex-shrink-0">场景</span>
              </div>
              <p class="text-sm text-gray-600 leading-relaxed">
                新人入职要花 2 周读历史项目文档，还搞不懂团队接口命名规则；跨团队协作时，代码风格差异导致合并冲突频发，沟通成本飙升。
              </p>
            </div>
            <div class="border-t border-gray-200 pt-4">
              <div class="flex items-start mb-2">
                <span class="inline-block bg-green-100 text-green-700 text-xs font-semibold px-2 py-1 rounded mr-2 flex-shrink-0">解决</span>
              </div>
              <p class="text-sm text-gray-700 leading-relaxed">
                AI 当"风格教练"，新人调用团队模板直接开发，跨团队时 AI 自动匹配目标风格，<span class="font-semibold text-green-600">上手周期从"周级"压缩到"小时级"</span>。
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Bottom Summary -->
      <div class="bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl p-8 text-center text-white shadow-2xl">
        <h4 class="text-2xl font-bold mb-3">码蜂如何解决？</h4>
        <p class="text-lg opacity-90 max-w-4xl mx-auto leading-relaxed">
          通过 <span class="font-bold border-b-2 border-white">官网在线制模</span> + <span class="font-bold border-b-2 border-white">轻量化插件检索</span>，
          让 AI 精准理解团队风格，历史代码即插即用，新人快速融入团队。
        </p>
      </div>
    </div>
  </section>
</template>
