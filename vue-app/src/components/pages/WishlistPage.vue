<template>
  <div class="pa-6 pa-sm-8" style="max-width: 1100px">
    <div class="d-flex flex-wrap align-start ga-4 mb-6">
      <div class="flex-grow-1">
        <div class="text-h6 mb-1">{{ t('wishlist.heading') }}</div>
        <p class="text-body-2 text-medium-emphasis mb-0">{{ t('wishlist.intro') }}</p>
      </div>

      <div class="d-flex ga-2">
        <v-btn-toggle
          v-model="view"
          density="comfortable"
          divided
          mandatory
          variant="outlined"
        >
          <v-btn :aria-label="t('wishlist.gridView')" icon="mdi-view-grid-outline" value="grid" />
          <v-btn :aria-label="t('wishlist.mapView')" icon="mdi-map-outline" value="map" />
        </v-btn-toggle>

        <v-btn color="primary" prepend-icon="mdi-plus" variant="elevated" @click="addDialog = true">
          {{ t('wishlist.add') }}
        </v-btn>
      </div>
    </div>

    <ApiErrorAlert :failure="destinationsStore.error" @close="destinationsStore.clearError()" />

    <div v-if="destinationsStore.loading" class="d-flex justify-center pa-12">
      <v-progress-circular color="primary" indeterminate />
    </div>

    <v-card v-else-if="destinationsStore.count === 0" class="pa-8 text-center" elevation="1">
      <v-icon class="mb-4" color="grey-lighten-1" size="48">mdi-map-marker-off-outline</v-icon>
      <div class="text-subtitle-1 mb-2">{{ t('wishlist.emptyTitle') }}</div>
      <p class="text-body-2 text-medium-emphasis mb-4">{{ t('wishlist.emptyBody') }}</p>
      <v-btn color="primary" variant="tonal" @click="addDialog = true">{{ t('wishlist.add') }}</v-btn>
    </v-card>

    <template v-else-if="view === 'map'">
      <v-card class="pa-4" elevation="1">
        <WorldMap :aria-label="t('wishlist.mapLabel')" :markers="markers" />

        <p class="text-caption text-medium-emphasis mt-3 mb-0">
          {{ t('wishlist.mapCaption', { count: destinationsStore.count }) }}
        </p>
      </v-card>
    </template>

    <v-row v-else>
      <v-col
        v-for="destination in destinationsStore.sorted"
        :key="destination.id"
        cols="12"
        md="4"
        sm="6"
      >
        <DestinationCard
          :destination="destination"
          :is-new="destinationsStore.remoteIds.includes(destination.id)"
          @remove="confirmRemove"
          @weather="goToWeather"
        />
      </v-col>
    </v-row>

    <v-dialog v-model="addDialog" max-width="520" @after-leave="resetAddForm">
      <v-card>
        <v-card-title class="text-subtitle-1 pa-4 pb-2">{{ t('wishlist.addTitle') }}</v-card-title>

        <v-card-text class="pa-4 pt-2">
          <CitySearchField ref="searchField" class="mb-4" @select="onCitySelected" />

          <v-expand-transition>
            <div v-if="picked">
              <v-alert class="mb-4" density="compact" type="info" variant="tonal">
                {{ picked.cityName }} — {{ picked.countryName ?? picked.countryCode }}
                ({{ picked.latitude.toFixed(3) }}, {{ picked.longitude.toFixed(3) }})
              </v-alert>

              <v-text-field
                v-model="addedBy"
                counter="100"
                density="comfortable"
                :hint="t('wishlist.yourNameHint')"
                :label="t('wishlist.yourName')"
                maxlength="100"
                persistent-hint
                prepend-inner-icon="mdi-account-outline"
              />
            </div>
          </v-expand-transition>
        </v-card-text>

        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="addDialog = false">{{ t('common.cancel') }}</v-btn>

          <v-btn
            color="primary"
            :disabled="!picked"
            :loading="destinationsStore.saving"
            variant="elevated"
            @click="submit"
          >
            {{ t('wishlist.add') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="removeDialog" max-width="420">
      <v-card>
        <v-card-title class="text-subtitle-1 pa-4 pb-2">{{ t('wishlist.removeTitle') }}</v-card-title>

        <v-card-text class="pa-4 pt-0 text-body-2">
          {{ t('wishlist.removeBody', { city: pendingRemoval?.cityName ?? '' }) }}
        </v-card-text>

        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="removeDialog = false">{{ t('common.cancel') }}</v-btn>
          <v-btn color="error" variant="elevated" @click="doRemove">{{ t('common.remove') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="addedSnack" color="success" timeout="3000">
      {{ t('wishlist.addedSnack', { city: lastAdded }) }}
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
  import type { CitySearchResult, Destination } from '@shared/types'
  import { computed, ref, useTemplateRef } from 'vue'
  import { useI18n } from 'vue-i18n'
  import ApiErrorAlert from '@/components/ApiErrorAlert.vue'
  import CitySearchField from '@/components/CitySearchField.vue'
  import DestinationCard from '@/components/DestinationCard.vue'
  import WorldMap from '@/components/WorldMap.vue'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'
  import { useUserStore } from '@/stores/useUserStore'

  const emit = defineEmits<{ navigate: [page: string, extra?: Record<string, string>] }>()

  const { t } = useI18n()
  const destinationsStore = useDestinationsStore()
  const userStore = useUserStore()

  const searchField = useTemplateRef<InstanceType<typeof CitySearchField>>('searchField')

  const view = ref<'grid' | 'map'>('grid')
  const addDialog = ref(false)
  const removeDialog = ref(false)
  const addedSnack = ref(false)
  const lastAdded = ref('')
  const picked = ref<CitySearchResult | null>(null)
  const pendingRemoval = ref<Destination | null>(null)
  const addedBy = ref(userStore.displayName)

  const markers = computed(() =>
    destinationsStore.items.map(d => ({
      lat: d.latitude,
      lon: d.longitude,
      label: `${d.cityName}, ${d.country?.name ?? d.country?.code}`,
    })),
  )

  function onCitySelected (city: CitySearchResult) {
    picked.value = city
  }

  function resetAddForm () {
    picked.value = null
    searchField.value?.reset()
  }

  async function submit () {
    if (!picked.value) return

    // Remember the name so the next add — and the Settings page — agree on it.
    userStore.setDisplayName(addedBy.value)

    const created = await destinationsStore.add({
      cityName: picked.value.cityName,
      countryCode: picked.value.countryCode,
      latitude: picked.value.latitude,
      longitude: picked.value.longitude,
      addedBy: userStore.attribution,
    })

    if (!created) return // store surfaced the error in the page-level alert
    lastAdded.value = created.cityName
    addedSnack.value = true
    addDialog.value = false
  }

  function confirmRemove (destination: Destination) {
    pendingRemoval.value = destination
    removeDialog.value = true
  }

  async function doRemove () {
    const target = pendingRemoval.value
    removeDialog.value = false
    pendingRemoval.value = null
    if (target) await destinationsStore.remove(target.id)
  }

  function goToWeather (destination: Destination) {
    emit('navigate', 'weather', { city: destination.cityName })
  }
</script>
