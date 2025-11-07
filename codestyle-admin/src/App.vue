<template>
  <a-config-provider>
    <SiteTopbar />
    <router-view />
  </a-config-provider>
</template>

<script lang="ts" setup>
  import SiteTopbar from '@/components/site-topbar.vue';
</script>
