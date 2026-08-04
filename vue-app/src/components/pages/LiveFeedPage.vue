<template>
  <div class="pa-6 pa-sm-8" style="max-width: 1000px">
    <div class="d-flex flex-wrap align-start ga-4 mb-6">
      <div class="flex-grow-1">
        <div class="text-h6 mb-1">{{ t('live.heading') }}</div>
        <p class="text-body-2 text-medium-emphasis mb-0">{{ t('live.intro') }}</p>
      </div>

      <div class="d-flex ga-2">
        <v-btn prepend-icon="mdi-restart" variant="outlined" @click="reconnect">
          {{ t('live.reconnect') }}
        </v-btn>

        <v-btn :disabled="log.length === 0" prepend-icon="mdi-notification-clear-all" variant="text" @click="clearLog">
          {{ t('live.clear') }}
        </v-btn>
      </div>
    </div>

    <v-card class="mb-6" elevation="1">
      <v-card-text class="pa-4 d-flex flex-wrap align-center ga-4">
        <LiveStatusChip />
        <code class="text-caption">{{ wsUrl }}</code>
        <v-spacer />

        <v-chip prepend-icon="mdi-map-marker-plus-outline" size="small" variant="tonal">
          {{ t('live.counts.destinations', { n: counts.destinations }) }}
        </v-chip>

        <v-chip prepend-icon="mdi-satellite-variant" size="small" variant="tonal">
          {{ t('live.counts.iss', { n: counts.iss }) }}
        </v-chip>
      </v-card-text>
    </v-card>

    <v-alert
      v-if="lastError"
      class="mb-6"
      density="compact"
      type="warning"
      variant="tonal"
    >
      {{ lastError }}
    </v-alert>

    <v-card elevation="1">
      <v-card-title class="text-subtitle-1 pa-4 pb-2 d-flex align-center ga-2">
        {{ t('live.feed') }}
        <v-spacer />

        <v-btn-toggle
          v-model="filter"
          density="compact"
          divided
          mandatory
          variant="outlined"
        >
          <v-btn size="small" value="all">{{ t('live.filters.all') }}</v-btn>
          <v-btn size="small" value="destinations">{{ t('live.filters.destinations') }}</v-btn>
          <v-btn size="small" value="iss">{{ t('live.filters.iss') }}</v-btn>
        </v-btn-toggle>
      </v-card-title>

      <v-divider />

      <div v-if="filtered.length === 0" class="pa-8 text-center">
        <v-icon class="mb-3" color="grey-lighten-1" size="40">mdi-timer-sand-empty</v-icon>
        <p class="text-body-2 text-medium-emphasis mb-0">{{ t('live.waiting') }}</p>
      </div>

      <v-list v-else class="py-0" density="compact">
        <template v-for="(entry, index) in filtered" :key="entry.id">
          <v-divider v-if="index > 0" />

          <v-list-item class="py-2">
            <template #prepend>
              <v-icon class="mr-3" :color="iconFor(entry).color">{{ iconFor(entry).icon }}</v-icon>
            </template>

            <v-list-item-title class="text-body-2">{{ summarize(entry) }}</v-list-item-title>

            <v-list-item-subtitle class="text-caption">
              <code>{{ JSON.stringify(entry.message) }}</code>
            </v-list-item-subtitle>

            <template #append>
              <span class="text-caption text-medium-emphasis">{{ clock(entry.receivedAt) }}</span>
            </template>
          </v-list-item>
        </template>
      </v-list>
    </v-card>

    <p class="text-caption text-medium-emphasis mt-3">{{ t('live.mergeNote') }}</p>
  </div>
</template>

<script setup lang="ts">
  import { isDestinationEvent } from '@shared/types'
  import { computed, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import LiveStatusChip from '@/components/LiveStatusChip.vue'
  import { useAppContext } from '@/composables/useAppContext'
  import { type LiveLogEntry, useLiveUpdates } from '@/composables/useLiveUpdates'

  const { t, locale } = useI18n()
  const { wsUrl } = useAppContext()
  const { log, lastError, reconnect, clearLog } = useLiveUpdates()

  const filter = ref<'all' | 'destinations' | 'iss'>('all')

  const filtered = computed(() =>
    log.value.filter(entry => {
      if (filter.value === 'all') return true
      const isDestination = isDestinationEvent(entry.message)
      return filter.value === 'destinations' ? isDestination : !isDestination
    }),
  )

  const counts = computed(() => ({
    destinations: log.value.filter(entry => isDestinationEvent(entry.message)).length,
    iss: log.value.filter(entry => !isDestinationEvent(entry.message)).length,
  }))

  const ACTION_ICONS = {
    CREATED: { icon: 'mdi-map-marker-plus-outline', color: 'success' },
    UPDATED: { icon: 'mdi-map-marker-check-outline', color: 'info' },
    DELETED: { icon: 'mdi-map-marker-remove-outline', color: 'error' },
  } as const

  function iconFor (entry: LiveLogEntry) {
    if (!isDestinationEvent(entry.message)) {
      return { icon: 'mdi-satellite-variant', color: 'secondary' }
    }
    return ACTION_ICONS[entry.message.action]
  }

  function summarize (entry: LiveLogEntry): string {
    const { message } = entry
    if (isDestinationEvent(message)) {
      return t(`live.events.${message.action}`, {
        city: message.destination.cityName,
        country: message.destination.countryCode,
      })
    }
    return t('live.events.ISS', {
      lat: message.latitude.toFixed(2),
      lon: message.longitude.toFixed(2),
    })
  }

  function clock (timestamp: number): string {
    return new Intl.DateTimeFormat(locale.value, {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    }).format(timestamp)
  }
</script>
