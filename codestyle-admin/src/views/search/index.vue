<template>
  <div style="padding: 40px; max-width: 1100px; margin: 0 auto">
    <a-typography-title :heading="3">搜索</a-typography-title>
    <a-typography-paragraph>
      搜索词：<b>{{ (route.query.q as string) || '' }}</b>
    </a-typography-paragraph>
  </div>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';

  const route = useRoute();
</script>
