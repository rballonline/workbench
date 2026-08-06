<template>
  <div class="pa-6 pa-sm-8" style="max-width: 900px">
    <div class="text-h6 mb-1">{{ t('explore.heading') }}</div>
    <p class="text-body-2 text-medium-emphasis mb-6">{{ t('explore.intro') }}</p>

    <v-card class="mb-6" elevation="1">
      <v-card-text class="pa-4">
        <v-text-field
          v-model="query"
          clearable
          density="comfortable"
          :hint="t('explore.searchHint')"
          :label="t('explore.searchLabel')"
          :loading="loading"
          persistent-hint
          prepend-inner-icon="mdi-magnify"
          @click:clear="clear"
          @keydown.enter.prevent="search"
        >
          <template #append>
            <v-btn
              color="primary"
              :disabled="query.trim().length < MIN_QUERY_LENGTH"
              :loading="loading"
              variant="tonal"
              @click="search"
            >
              {{ t('common.search') }}
            </v-btn>
          </template>
        </v-text-field>
      </v-card-text>
    </v-card>

    <ApiErrorAlert :failure="error" @close="error = null" />

    <v-card
      v-if="searched && !loading && results.length === 0 && !error"
      class="pa-8 text-center"
      elevation="1"
    >
      <v-icon class="mb-4" color="grey-lighten-1" size="48">mdi-map-search-outline</v-icon>
      <div class="text-subtitle-1 mb-2">{{ t('explore.noResultsTitle') }}</div>
      <p class="text-body-2 text-medium-emphasis mb-0">{{ t('explore.noResultsBody') }}</p>
    </v-card>

    <v-card v-if="results.length > 0" elevation="1">
      <v-list lines="two">
        <template v-for="(city, index) in results" :key="keyOf(city)">
          <v-divider v-if="index > 0" />

          <v-list-item class="py-2">
            <template #prepend>
              <v-avatar v-if="city.flagUrl" class="mr-3" rounded="sm" size="36">
                <v-img :alt="city.countryName ?? city.countryCode" :src="city.flagUrl" />
              </v-avatar>

              <v-avatar
                v-else
                class="mr-3"
                color="surface-variant"
                rounded="sm"
                size="36"
              >
                <v-icon>mdi-flag-outline</v-icon>
              </v-avatar>
            </template>

            <v-list-item-title>{{ city.cityName }}</v-list-item-title>

            <v-list-item-subtitle>
              {{ [city.countryName ?? city.countryCode, city.region, city.capital]
                .filter(Boolean).join(' · ') }}
            </v-list-item-subtitle>

            <template #append>
              <div class="d-flex align-center ga-2">
                <span class="text-caption text-medium-emphasis d-none d-sm-inline">
                  {{ city.latitude.toFixed(2) }}, {{ city.longitude.toFixed(2) }}
                </span>

                <v-btn
                  :aria-label="t('wishlist.checkWeather')"
                  icon="mdi-weather-partly-cloudy"
                  size="small"
                  variant="text"
                  @click="router.push({ name: 'weather', query: { city: city.cityName } })"
                />

                <v-btn
                  color="primary"
                  :disabled="isOnWishlist(city)"
                  :loading="addingKey === keyOf(city)"
                  size="small"
                  variant="tonal"
                  @click="add(city)"
                >
                  {{ isOnWishlist(city) ? t('explore.onWishlist') : t('explore.addToWishlist') }}
                </v-btn>
              </div>
            </template>
          </v-list-item>
        </template>
      </v-list>
    </v-card>

    <v-snackbar v-model="addedSnack" color="success" timeout="3000">
      {{ t('wishlist.addedSnack', { city: lastAdded }) }}
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
  import type { CitySearchResult } from '@shared/types'
  import { ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useRouter } from 'vue-router'
  import ApiErrorAlert from '@/components/ApiErrorAlert.vue'
  import { type ApiFailure, classifyError, useApi } from '@/composables/useApi'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'
  import { useUserStore } from '@/stores/useUserStore'

  /** Matches `CitySearchRequest`'s `@Size(min = 2)`. */
  const MIN_QUERY_LENGTH = 2

  const { t } = useI18n()
  const router = useRouter()
  const api = useApi()
  const destinationsStore = useDestinationsStore()
  const userStore = useUserStore()

  const query = ref('')
  const results = ref<CitySearchResult[]>([])
  const loading = ref(false)
  const searched = ref(false)
  const error = ref<ApiFailure | null>(null)
  const addingKey = ref<string | null>(null)
  const addedSnack = ref(false)
  const lastAdded = ref('')

  function keyOf (city: CitySearchResult): string {
    return `${city.cityName}|${city.countryCode}|${city.latitude},${city.longitude}`
  }

  /** Coordinates rather than name, since "Springfield" resolves many ways. */
  function isOnWishlist (city: CitySearchResult): boolean {
    return destinationsStore.items.some(
      d =>
        Math.abs(d.latitude - city.latitude) < 0.01
        && Math.abs(d.longitude - city.longitude) < 0.01,
    )
  }

  function clear () {
    results.value = []
    searched.value = false
    error.value = null
  }

  async function search () {
    const term = query.value.trim()
    if (term.length < MIN_QUERY_LENGTH) return

    loading.value = true
    error.value = null
    try {
      results.value = await api.searchCities(term)
      searched.value = true
    } catch (error_) {
      results.value = []
      error.value = classifyError(error_)
    } finally {
      loading.value = false
    }
  }

  async function add (city: CitySearchResult) {
    addingKey.value = keyOf(city)
    const created = await destinationsStore.add({
      cityName: city.cityName,
      countryCode: city.countryCode,
      latitude: city.latitude,
      longitude: city.longitude,
      addedBy: userStore.attribution,
    })
    addingKey.value = null

    if (created) {
      lastAdded.value = created.cityName
      addedSnack.value = true
    } else {
      error.value = destinationsStore.error
    }
  }
</script>
