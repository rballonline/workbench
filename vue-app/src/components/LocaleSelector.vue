<template>
  <v-menu>
    <template #activator="{ props }">
      <v-btn v-bind="props" :aria-label="t('common.language')" density="comfortable" variant="text">
        {{ activeLocale.flag }}
      </v-btn>
    </template>

    <v-list density="compact">
      <v-list-item
        v-for="option in SUPPORTED_LOCALES"
        :key="option.code"
        :active="option.code === userStore.locale"
        :title="`${option.flag}  ${option.label}`"
        @click="userStore.setLocale(option.code)"
      />
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { SUPPORTED_LOCALES } from '@/plugins/i18n'
  import { useUserStore } from '@/stores/useUserStore'

  const { t } = useI18n()
  const userStore = useUserStore()

  const activeLocale = computed(
    () => SUPPORTED_LOCALES.find(l => l.code === userStore.locale) ?? SUPPORTED_LOCALES[0],
  )
</script>
