<template>
  <div class="pa-6 pa-sm-8" style="max-width: 900px">
    <div class="text-h6 mb-1">{{ t('countries.heading') }}</div>
    <p class="text-body-2 text-medium-emphasis mb-6">{{ t('countries.intro') }}</p>

    <v-card class="mb-6" elevation="1">
      <v-card-text class="pa-4">
        <v-text-field
          v-model="name"
          density="comfortable"
          :hint="t('countries.nameHint')"
          :label="t('countries.nameLabel')"
          :loading="loading"
          persistent-hint
          prepend-inner-icon="mdi-flag-outline"
          @keydown.enter.prevent="lookup"
        >
          <template #append>
            <v-btn
              color="primary"
              :disabled="!name.trim()"
              :loading="loading"
              variant="tonal"
              @click="lookup"
            >
              {{ t('common.search') }}
            </v-btn>
          </template>
        </v-text-field>

        <div v-if="wishlistCountries.length > 0" class="d-flex flex-wrap ga-2 mt-4">
          <span class="text-caption text-medium-emphasis align-self-center mr-1">
            {{ t('countries.fromWishlist') }}
          </span>

          <v-chip
            v-for="suggestion in wishlistCountries"
            :key="suggestion"
            size="small"
            variant="outlined"
            @click="lookupName(suggestion)"
          >
            {{ suggestion }}
          </v-chip>
        </div>
      </v-card-text>
    </v-card>

    <ApiErrorAlert
      :failure="error"
      :not-found="t('countries.notFound', { name: lastQueried })"
      @close="error = null"
    />

    <v-card v-if="country" elevation="1">
      <div class="d-flex flex-wrap ga-6 pa-6">
        <v-img
          v-if="country.flags?.png"
          :alt="commonName"
          class="flex-grow-0 rounded border"
          max-width="160"
          :src="country.flags.png"
          width="160"
        />

        <div class="flex-grow-1">
          <div class="text-h6 mb-1">{{ commonName }}</div>

          <div class="text-caption text-medium-emphasis mb-4">
            {{ country.cca2?.[0] ?? '—' }} · {{ country.region ?? t('countries.unknownRegion') }}
          </div>

          <v-row dense>
            <v-col v-for="fact in facts" :key="fact.labelKey" cols="12" sm="6">
              <div class="text-caption text-medium-emphasis">{{ t(fact.labelKey) }}</div>
              <div class="text-body-2">{{ fact.value }}</div>
            </v-col>
          </v-row>
        </div>
      </div>

      <v-divider />

      <v-card-text class="py-3">
        <span class="text-caption text-medium-emphasis">{{ t('countries.passthroughNote') }}</span>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
  import type { CountryApiResult } from '@shared/types'
  import { computed, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import ApiErrorAlert from '@/components/ApiErrorAlert.vue'
  import { type ApiFailure, classifyError, useApi } from '@/composables/useApi'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'

  const { t, locale } = useI18n()
  const api = useApi()
  const destinationsStore = useDestinationsStore()

  const name = ref('')
  const country = ref<CountryApiResult | null>(null)
  const loading = ref(false)
  const error = ref<ApiFailure | null>(null)
  /** Held separately so a 404 message keeps naming the term that actually failed. */
  const lastQueried = ref('')

  const wishlistCountries = computed(() =>
    [...new Set(destinationsStore.items.map(d => d.country?.name).filter(Boolean))] as string[],
  )

  // `/api/countries/{name}` returns the raw REST Countries shape, where the display
  // name is nested under `name.common` rather than being a flat field.
  const commonName = computed(() => country.value?.name?.common ?? name.value)

  const facts = computed(() => [
    {
      labelKey: 'countries.capital',
      value: country.value?.capital?.[0] ?? '—',
    },
    {
      labelKey: 'countries.population',
      value: country.value?.population == null
        ? '—'
        : new Intl.NumberFormat(locale.value).format(country.value.population),
    },
    {
      labelKey: 'countries.region',
      value: country.value?.region ?? '—',
    },
    {
      labelKey: 'countries.onWishlist',
      value: String(
        destinationsStore.items.filter(d => d.country?.name === commonName.value).length,
      ),
    },
  ])

  async function lookup () {
    const term = name.value.trim()
    if (!term) return

    loading.value = true
    error.value = null
    lastQueried.value = term
    try {
      country.value = await api.getCountry(term)
    } catch (error_) {
      country.value = null
      error.value = classifyError(error_)
    } finally {
      loading.value = false
    }
  }

  function lookupName (value: string) {
    name.value = value
    lookup()
  }
</script>
