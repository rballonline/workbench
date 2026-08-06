<template>
  <div class="pa-6 pa-sm-8" style="max-width: 1100px">
    <div class="d-flex flex-wrap align-start ga-4 mb-6">
      <div class="flex-grow-1">
        <div class="text-h6 mb-1">{{ t("iss.heading") }}</div>

        <p class="text-body-2 text-medium-emphasis mb-0">
          {{ t("iss.intro") }}
        </p>
      </div>

      <div class="d-flex ga-2">
        <v-btn
          :loading="issStore.loading"
          prepend-icon="mdi-refresh"
          variant="outlined"
          @click="issStore.fetchOnce()"
        >
          {{ t("iss.pollNow") }}
        </v-btn>

        <v-btn
          :disabled="issStore.trail.length <= 1"
          prepend-icon="mdi-eraser"
          variant="text"
          @click="issStore.clearTrail()"
        >
          {{ t("iss.clearTrail") }}
        </v-btn>
      </div>
    </div>

    <ApiErrorAlert :failure="issStore.error" @close="issStore.error = null" />

    <v-row class="mb-2">
      <v-col v-for="stat in stats" :key="stat.labelKey" cols="6" md="3">
        <v-card class="h-100" elevation="1">
          <v-card-text class="pa-4">
            <div class="text-caption text-medium-emphasis mb-1">
              {{ t(stat.labelKey) }}
            </div>

            <div class="text-h6">{{ stat.value }}</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="pa-4" elevation="1">
      <WorldMap
        :aria-label="t('iss.mapLabel')"
        :markers="markers"
        :trails="trails"
      />

      <div class="d-flex flex-wrap align-center ga-4 mt-3">
        <div class="d-flex align-center ga-2 text-caption text-medium-emphasis">
          <span class="legend-dot bg-primary" />{{ t("iss.legendIss") }}
        </div>

        <div
          v-if="destinationsStore.count > 0"
          class="d-flex align-center ga-2 text-caption text-medium-emphasis"
        >
          <span class="legend-dot bg-secondary" />{{ t("iss.legendWishlist") }}
        </div>

        <v-spacer />

        <span class="text-caption text-medium-emphasis">{{
          t("iss.pushNote")
        }}</span>
      </div>
    </v-card>

    <v-card v-if="nearest" class="mt-6" elevation="1">
      <v-card-text class="pa-4 d-flex flex-wrap align-center ga-3">
        <v-icon color="secondary">mdi-map-marker-distance</v-icon>

        <div class="flex-grow-1">
          <div class="text-body-2">
            {{ t("iss.nearest", { city: nearest.destination.cityName }) }}
          </div>

          <div class="text-caption text-medium-emphasis">
            {{ t("iss.nearestDistance", { km: formatNumber(nearest.km) }) }}
          </div>
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
  import type { MapMarker } from '@/components/WorldMap.types'
  import type { Destination } from '@shared/types'
  import { computed, onMounted, onUnmounted, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import ApiErrorAlert from '@/components/ApiErrorAlert.vue'
  import WorldMap from '@/components/WorldMap.vue'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'
  import { useIssStore } from '@/stores/useIssStore'

  const EARTH_RADIUS_KM = 6371

  const { t, locale } = useI18n()
  const issStore = useIssStore()
  const destinationsStore = useDestinationsStore()

  function formatNumber (value: number): string {
    return new Intl.NumberFormat(locale.value, {
      maximumFractionDigits: 0,
    }).format(value)
  }

  const markers = computed<MapMarker[]>(() => {
    const points: MapMarker[] = destinationsStore.items.map(d => ({
      lat: d.latitude,
      lon: d.longitude,
      label: d.cityName,
      color: 'rgb(var(--v-theme-secondary))',
      size: 1.6,
    }))

    if (issStore.position) {
      points.push({
        lat: issStore.position.latitude,
        lon: issStore.position.longitude,
        label: t('iss.heading'),
        color: 'rgb(var(--v-theme-primary))',
        size: 3,
        pulse: true,
      })
    }
    return points
  })

  const trails = computed(() => [{ segments: issStore.trailSegments }])

  const coordinates = computed(() => {
    const position = issStore.position
    if (!position) return '-'
    const { latitude: lat, longitude: lon } = position
    return `${Math.abs(lat).toFixed(2)}°${lat >= 0 ? 'N' : 'S'}, ${Math.abs(lon).toFixed(2)}°${lon >= 0 ? 'E' : 'W'}`
  })

  const hemisphere = computed(() =>
    (issStore.position?.latitude ?? 0) >= 0 ? 'north' : 'south',
  )

  // issStore.position.timestamp doesn't change between samples, so a plain
  // computed based on it would never re-evaluate; this tick forces one every second.
  const now = ref(Date.now())
  let clockId: ReturnType<typeof setInterval> | undefined

  const ageSeconds = computed(() => {
    const position = issStore.position
    if (!position) return null
    return Math.max(0, Math.round(now.value / 1000 - position.timestamp))
  })

  const stats = computed(() => [
    { labelKey: 'iss.stats.position', value: coordinates.value },
    {
      labelKey: 'iss.stats.age',
      value:
        ageSeconds.value == null
          ? '-'
          : t('iss.secondsAgo', { n: ageSeconds.value }),
    },
    { labelKey: 'iss.stats.samples', value: String(issStore.trail.length) },
    {
      labelKey: 'iss.stats.hemisphere',
      value: issStore.position ? t(`iss.hemisphere.${hemisphere.value}`) : '-',
    },
  ])

  /** Great-circle distance, so the "closest wishlist city" readout is honest. */
  function haversineKm (
    aLat: number,
    aLon: number,
    bLat: number,
    bLon: number,
  ): number {
    const toRad = (deg: number) => (deg * Math.PI) / 180
    const dLat = toRad(bLat - aLat)
    const dLon = toRad(bLon - aLon)
    const h
      = Math.sin(dLat / 2) ** 2
        + Math.cos(toRad(aLat)) * Math.cos(toRad(bLat)) * Math.sin(dLon / 2) ** 2
    return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h))
  }

  const nearest = computed<{ destination: Destination, km: number } | null>(
    () => {
      const position = issStore.position
      if (!position || destinationsStore.count === 0) return null

      return destinationsStore.items.reduce<{
        destination: Destination
        km: number
      } | null>((best, destination) => {
        const km = haversineKm(
          position.latitude,
          position.longitude,
          destination.latitude,
          destination.longitude,
        )
        return best == null || km < best.km ? { destination, km } : best
      }, null)
    },
  )

  // The WebSocket pushes a fresh position every 5 seconds, but a one-shot REST read
  // means the map is populated immediately instead of after the first tick.
  onMounted(() => {
    if (!issStore.position) issStore.fetchOnce()
    clockId = setInterval(() => {
      now.value = Date.now()
    }, 1000)
  })

  onUnmounted(() => {
    clearInterval(clockId)
  })
</script>

<style scoped>
.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}
</style>
