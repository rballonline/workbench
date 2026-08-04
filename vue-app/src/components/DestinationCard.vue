<template>
  <v-card class="h-100 d-flex flex-column" elevation="1">
    <div class="d-flex align-center ga-3 pa-4 pb-2">
      <v-avatar v-if="destination.country?.flagUrl" rounded="sm" size="40">
        <v-img :alt="destination.country.name" :src="destination.country.flagUrl" />
      </v-avatar>

      <v-avatar v-else color="surface-variant" rounded="sm" size="40">
        <v-icon>mdi-flag-outline</v-icon>
      </v-avatar>

      <div class="flex-grow-1 min-width-0">
        <div class="text-subtitle-1 text-truncate">{{ destination.cityName }}</div>

        <div class="text-caption text-medium-emphasis text-truncate">
          {{ destination.country?.name ?? destination.country?.code }}
          <template v-if="destination.country?.region"> · {{ destination.country.region }}</template>
        </div>
      </div>

      <v-chip v-if="isNew" color="success" size="x-small" variant="tonal">
        {{ t('wishlist.justAdded') }}
      </v-chip>
    </div>

    <v-card-text class="pt-1 pb-2 flex-grow-1">
      <div class="d-flex flex-wrap ga-2 mb-3">
        <v-chip prepend-icon="mdi-crosshairs-gps" size="x-small" variant="outlined">
          {{ coordinates }}
        </v-chip>

        <v-chip
          v-if="destination.country?.capital"
          prepend-icon="mdi-star-outline"
          size="x-small"
          variant="outlined"
        >
          {{ destination.country.capital }}
        </v-chip>

        <v-chip
          v-if="destination.country?.population"
          prepend-icon="mdi-account-group-outline"
          size="x-small"
          variant="outlined"
        >
          {{ population }}
        </v-chip>
      </div>

      <div class="text-caption text-medium-emphasis">
        {{ destination.addedBy
          ? t('wishlist.addedByOn', { name: destination.addedBy, date: addedOn })
          : t('wishlist.addedOn', { date: addedOn }) }}
      </div>
    </v-card-text>

    <v-card-actions class="px-4 pb-3 pt-0">
      <v-btn
        prepend-icon="mdi-weather-partly-cloudy"
        size="small"
        variant="text"
        @click="$emit('weather', destination)"
      >
        {{ t('wishlist.checkWeather') }}
      </v-btn>

      <v-spacer />

      <v-btn
        :aria-label="t('common.remove')"
        color="error"
        icon="mdi-trash-can-outline"
        size="small"
        variant="text"
        @click="$emit('remove', destination)"
      />
    </v-card-actions>
  </v-card>
</template>

<script setup lang="ts">
  import type { Destination } from '@shared/types'
  import { computed } from 'vue'
  import { useI18n } from 'vue-i18n'

  const props = defineProps<{ destination: Destination, isNew?: boolean }>()
  defineEmits<{ remove: [destination: Destination], weather: [destination: Destination] }>()

  const { t, locale } = useI18n()

  const coordinates = computed(() => {
    const { latitude: lat, longitude: lon } = props.destination
    return `${Math.abs(lat).toFixed(2)}°${lat >= 0 ? 'N' : 'S'}, ${Math.abs(lon).toFixed(2)}°${lon >= 0 ? 'E' : 'W'}`
  })

  const population = computed(() =>
    new Intl.NumberFormat(locale.value, { notation: 'compact' })
      .format(props.destination.country?.population ?? 0),
  )

  const addedOn = computed(() => {
    // Spring serializes LocalDateTime without a zone; treat it as local time.
    const parsed = new Date(props.destination.createdAt)
    return Number.isNaN(parsed.getTime())
      ? '—'
      : new Intl.DateTimeFormat(locale.value, { dateStyle: 'medium' }).format(parsed)
  })
</script>

<style scoped>
  .min-width-0 {
    min-width: 0;
  }
</style>
