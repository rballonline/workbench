<template>
  <div class="pa-6 pa-sm-8" style="max-width: 1100px">
    <div class="text-h6 mb-1">{{ t('home.heading') }}</div>
    <p class="text-body-2 text-medium-emphasis mb-6">{{ t('home.intro') }}</p>

    <ApiErrorAlert :failure="destinationsStore.error" @close="destinationsStore.clearError()" />

    <v-row class="mb-2">
      <v-col v-for="stat in stats" :key="stat.labelKey" cols="6" md="3">
        <v-card class="h-100" elevation="1">
          <v-card-text class="pa-4">
            <div class="d-flex align-center ga-2 mb-1">
              <v-icon :color="stat.color" size="18">{{ stat.icon }}</v-icon>
              <span class="text-caption text-medium-emphasis">{{ t(stat.labelKey) }}</span>
            </div>

            <div class="text-h5">{{ stat.value }}</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-row>
      <v-col
        v-for="action in ACTIONS"
        :key="action.page"
        cols="12"
        md="4"
        sm="6"
      >
        <v-card class="h-100 cursor-pointer" elevation="1" hover @click="router.push({ name: action.page })">
          <v-card-text class="d-flex flex-column align-center text-center pa-6">
            <v-icon class="mb-3" color="primary" size="40">{{ action.icon }}</v-icon>
            <div class="text-subtitle-2 mb-1">{{ t(`nav.${action.page}`) }}</div>
            <p class="text-caption text-medium-emphasis">{{ t(`home.cards.${action.page}`) }}</p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-card v-if="regions.length > 0" class="mt-6" elevation="1">
      <v-card-title class="text-subtitle-1 pa-4 pb-2">{{ t('home.byRegion') }}</v-card-title>

      <v-card-text class="pa-4 pt-0">
        <div v-for="region in regions" :key="region.name" class="mb-3">
          <div class="d-flex justify-space-between text-caption mb-1">
            <span>{{ region.name }}</span>
            <span class="text-medium-emphasis">{{ region.count }}</span>
          </div>

          <v-progress-linear
            color="primary"
            height="6"
            :model-value="(region.count / destinationsStore.count) * 100"
            rounded
          />
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useRouter } from 'vue-router'
  import ApiErrorAlert from '@/components/ApiErrorAlert.vue'
  import { useLiveUpdates } from '@/composables/useLiveUpdates'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'

  const { t } = useI18n()
  const router = useRouter()
  const destinationsStore = useDestinationsStore()
  const { status } = useLiveUpdates()

  const ACTIONS = [
    { page: 'wishlist', icon: 'mdi-map-marker-multiple-outline' },
    { page: 'explore', icon: 'mdi-magnify' },
    { page: 'weather', icon: 'mdi-weather-partly-cloudy' },
    { page: 'countries', icon: 'mdi-flag-outline' },
    { page: 'iss', icon: 'mdi-satellite-variant' },
    { page: 'live', icon: 'mdi-pulse' },
  ] as const

  const countryCount = computed(
    () => new Set(destinationsStore.items.map(d => d.country?.code).filter(Boolean)).size,
  )

  const stats = computed(() => [
    {
      labelKey: 'home.stats.destinations',
      value: destinationsStore.count,
      icon: 'mdi-map-marker-outline',
      color: 'primary',
    },
    {
      labelKey: 'home.stats.countries',
      value: countryCount.value,
      icon: 'mdi-flag-outline',
      color: 'secondary',
    },
    {
      labelKey: 'home.stats.contributors',
      value: destinationsStore.contributors.length,
      icon: 'mdi-account-multiple-outline',
      color: 'teal',
    },
    {
      labelKey: 'home.stats.stream',
      value: t(`live.status.${status.value}`),
      icon: 'mdi-pulse',
      color: status.value === 'open' ? 'success' : 'warning',
    },
  ])

  const regions = computed(() =>
    Object.entries(destinationsStore.byRegion)
      .map(([name, items]) => ({ name, count: items.length }))
      .toSorted((a, b) => b.count - a.count),
  )
</script>
