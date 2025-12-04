<script setup lang="ts">
const ides = [
  { name: 'Cursor', logo: '/assets/image/cursor.webp' },
  { name: 'Windsurf', logo: '/assets/image/windsurf.webp' },
  { name: 'Trae', logo: '/assets/image/trae-new-logo.jpg' },
  { name: 'Cline', logo: '/assets/image/cline.png' },
]
</script>

<template>
  <section class="py-16 px-6 lg:px-8 bg-white">
    <div class="max-w-6xl mx-auto">
      <!-- Section Title -->
      <h2 class="text-3xl md:text-4xl font-bold text-center text-gray-900 mb-12">
        支持主流 AI IDE
      </h2>

      <!-- IDE Grid -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
        <div 
          v-for="ide in ides" 
          :key="ide.name"
          class="flex flex-col items-center space-y-4"
        >
          <div class="w-32 h-32 rounded-2xl bg-white shadow-lg flex items-center justify-center p-4 border border-gray-100 hover:shadow-xl transition-all hover:-translate-y-2 group">
            <img :src="ide.logo" :alt="ide.name" class="w-full h-full object-contain group-hover:scale-110 transition-transform duration-300" />
          </div>
          <span class="text-lg font-semibold text-gray-700">{{ ide.name }}</span>
        </div>
      </div>
    </div>
  </section>
</template>
