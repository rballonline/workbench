<template>
  <AppShell />
</template>

<script lang="ts" setup>
  import { defineAsyncComponent } from 'vue'

  const AppShell = defineAsyncComponent(() => import('@/components/AppShell.vue'))
</script>
