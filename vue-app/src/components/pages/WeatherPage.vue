<template>
  <div class="pa-6 pa-sm-8" style="max-width: 900px">
    <div class="text-h6 mb-1">{{ t("weather.heading") }}</div>

    <p class="text-body-2 text-medium-emphasis mb-6">
      {{ t("weather.intro") }}
    </p>

    <v-card class="mb-6" elevation="1">
      <v-card-text class="pa-4">
        <v-text-field
          v-model="city"
          density="comfortable"
          hide-details="auto"
          :label="t('weather.cityLabel')"
          :loading="loading"
          prepend-inner-icon="mdi-city-variant-outline"
          @keydown.enter.prevent="lookup"
        >
          <template #append>
            <v-btn
              color="primary"
              :disabled="!city.trim()"
              :loading="loading"
              variant="tonal"
              @click="lookup"
            >
              {{ t("common.search") }}
            </v-btn>
          </template>
        </v-text-field>

        <div
          v-if="destinationsStore.count > 0"
          class="d-flex flex-wrap ga-2 mt-4"
        >
          <span
            class="text-caption text-medium-emphasis align-self-center mr-1"
          >
            {{ t("weather.fromWishlist") }}
          </span>

          <v-chip
            v-for="destination in destinationsStore.sorted.slice(0, 8)"
            :key="destination.id"
            size="small"
            variant="outlined"
            @click="lookupCity(destination.cityName)"
          >
            {{ destination.cityName }}
          </v-chip>
        </div>
      </v-card-text>
    </v-card>

    <ApiErrorAlert
      :failure="error"
      :not-found="t('weather.notFound', { city: lastQueried })"
      @close="error = null"
    />

    <v-card v-if="weather" elevation="1">
      <v-card-text class="pa-6">
        <div class="d-flex flex-wrap align-center ga-6">
          <div class="d-flex align-center ga-4">
            <v-icon :color="tempColor" size="64">{{ descriptor.icon }}</v-icon>

            <div>
              <div class="text-h4">{{ temperature }}</div>

              <div class="text-body-2 text-medium-emphasis">
                {{ t(`weather.codes.${descriptor.key}`) }}
              </div>
            </div>
          </div>

          <v-divider class="d-none d-sm-block" vertical />

          <div class="flex-grow-1">
            <div class="text-subtitle-1">{{ weather.cityName }}</div>

            <div class="text-caption text-medium-emphasis mb-2">
              {{ weather.countryName ?? t("weather.unknownCountry") }} ·
              {{ weather.latitude.toFixed(2) }},
              {{ weather.longitude.toFixed(2) }}
            </div>

            <div class="d-flex flex-wrap ga-2">
              <v-chip
                prepend-icon="mdi-weather-windy"
                size="small"
                variant="tonal"
              >
                {{
                  weather.windSpeedKmh != null
                    ? `${weather.windSpeedKmh.toFixed(0)} km/h`
                    : "-"
                }}
              </v-chip>

              <v-chip
                prepend-icon="mdi-water-percent"
                size="small"
                variant="tonal"
              >
                {{ weather.humidity != null ? `${weather.humidity}%` : "-" }}
              </v-chip>

              <v-chip
                prepend-icon="mdi-thermometer"
                size="small"
                variant="tonal"
              >
                {{ fahrenheit }}
              </v-chip>
            </div>
          </div>
        </div>
      </v-card-text>

      <v-divider />

      <v-card-actions class="px-4 py-3">
        <span class="text-caption text-medium-emphasis">
          {{ t("weather.chainNote") }}
        </span>

        <v-spacer />

        <v-btn
          v-if="!onWishlist"
          prepend-icon="mdi-plus"
          size="small"
          variant="text"
          @click="router.push({ name: 'explore' })"
        >
          {{ t("explore.addToWishlist") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script setup lang="ts">
  import type { Weather } from '@shared/types'
  import { computed, onMounted, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useRoute, useRouter } from 'vue-router'
  import ApiErrorAlert from '@/components/ApiErrorAlert.vue'
  import { type ApiFailure, classifyError, useApi } from '@/composables/useApi'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'
  import {
    describeWeatherCode,
    temperatureColor,
    toFahrenheit,
  } from '@/utils/weatherCodes'

  const { t } = useI18n()
  const route = useRoute()
  const router = useRouter()
  const api = useApi()
  const destinationsStore = useDestinationsStore()

  const city = ref('')
  const weather = ref<Weather | null>(null)
  const loading = ref(false)
  const error = ref<ApiFailure | null>(null)
  /** Held separately so a 404 message keeps naming the term that actually failed. */
  const lastQueried = ref('')

  const descriptor = computed(() =>
    describeWeatherCode(weather.value?.weatherCode),
  )
  const tempColor = computed(() =>
    temperatureColor(weather.value?.temperatureCelsius),
  )

  const temperature = computed(() =>
    weather.value?.temperatureCelsius == null
      ? '-'
      : `${weather.value.temperatureCelsius.toFixed(1)}°C`,
  )

  const fahrenheit = computed(() =>
    weather.value?.temperatureCelsius == null
      ? '-'
      : `${toFahrenheit(weather.value.temperatureCelsius).toFixed(1)}°F`,
  )

  const onWishlist = computed(
    () =>
      weather.value != null
      && destinationsStore.items.some(d => d.cityName === weather.value?.cityName),
  )

  async function lookup () {
    const name = city.value.trim()
    if (!name) return

    loading.value = true
    error.value = null
    lastQueried.value = name
    try {
      weather.value = await api.getWeather(name)
    } catch (error_) {
      weather.value = null
      error.value = classifyError(error_)
    } finally {
      loading.value = false
    }
  }

  function lookupCity (name: string) {
    city.value = name
    lookup()
  }

  // Deep link from the wishlist card's "check weather" action.
  onMounted(() => {
    const preset = route.query.city
    if (typeof preset === 'string') lookupCity(preset)
  })
</script>
