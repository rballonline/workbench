<template>
  <v-chip
    :color="appearance.color"
    :prepend-icon="appearance.icon"
    size="small"
    variant="tonal"
  >
    {{ t(`live.status.${status}`) }}
    <v-tooltip activator="parent" location="bottom">
      {{ lastError ?? t('live.streamTooltip') }}
    </v-tooltip>
  </v-chip>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useLiveUpdates } from '@/composables/useLiveUpdates'

  const { t } = useI18n()
  const { status, lastError } = useLiveUpdates()

  const APPEARANCE = {
    idle: { color: 'grey', icon: 'mdi-circle-outline' },
    connecting: { color: 'warning', icon: 'mdi-lan-pending' },
    open: { color: 'success', icon: 'mdi-lan-connect' },
    closed: { color: 'error', icon: 'mdi-lan-disconnect' },
  } as const

  const appearance = computed(() => APPEARANCE[status.value])
</script>
