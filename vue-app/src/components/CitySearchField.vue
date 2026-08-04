<template>
  <v-autocomplete
    v-model="selected"
    v-model:search="query"
    auto-select-first
    clearable
    :custom-filter="() => true"
    density="comfortable"
    :error-messages="error ? [error] : []"
    :hint="hint"
    item-title="cityName"
    :item-value="keyOf"
    :items="results"
    :label="label ?? t('explore.searchLabel')"
    :loading="loading"
    no-filter
    persistent-hint
    prepend-inner-icon="mdi-city-variant-outline"
    return-object
    @update:model-value="onSelect"
  >
    <template #item="{ props: itemProps, item }">
      <v-list-item
        v-bind="itemProps"
        :subtitle="describe(item.raw)"
        :title="item.raw.cityName"
      >
        <template #prepend>
          <v-avatar v-if="item.raw.flagUrl" class="mr-2" rounded="0" size="28">
            <v-img :alt="item.raw.countryName ?? item.raw.countryCode" :src="item.raw.flagUrl" />
          </v-avatar>

          <v-avatar v-else class="mr-2" rounded="0" size="28">
            <v-icon size="20">mdi-flag-outline</v-icon>
          </v-avatar>
        </template>
      </v-list-item>
    </template>

    <template #no-data>
      <div class="px-4 py-3 text-caption text-medium-emphasis">
        {{ query.length < MIN_QUERY_LENGTH ? t('explore.typeMore') : t('explore.noMatches') }}
      </div>
    </template>
  </v-autocomplete>
</template>

<script setup lang="ts">
  import type { CitySearchResult } from '@shared/types'
  import { computed, onUnmounted, ref, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { classifyError, useApi } from '@/composables/useApi'

  /** `CitySearchRequest.q` is `@Size(min = 2)`; searching sooner just earns a 400. */
  const MIN_QUERY_LENGTH = 2
  const DEBOUNCE_MS = 300

  const props = defineProps<{ label?: string }>()
  const emit = defineEmits<{ select: [city: CitySearchResult] }>()

  const { t } = useI18n()
  const api = useApi()

  const query = ref('')
  const results = ref<CitySearchResult[]>([])
  const selected = ref<CitySearchResult | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  let debounceTimer: ReturnType<typeof setTimeout> | null = null
  // Monotonic token so a slow earlier request can't overwrite a newer result set.
  let requestToken = 0

  const hint = computed(() => (props.label ? '' : t('explore.searchHint')))

  /** Cities are not unique by name, so key on the coordinates the backend returned. */
  function keyOf (city: CitySearchResult): string {
    return `${city.cityName}|${city.countryCode}|${city.latitude},${city.longitude}`
  }

  function describe (city: CitySearchResult): string {
    return [city.countryName ?? city.countryCode, city.region].filter(Boolean).join(' · ')
  }

  async function run (term: string) {
    const token = ++requestToken
    loading.value = true
    error.value = null
    try {
      const found = await api.searchCities(term)
      if (token === requestToken) results.value = found
    } catch (error_) {
      if (token !== requestToken) return
      results.value = []
      error.value = t(`common.errors.${classifyError(error_).kind}`)
    } finally {
      if (token === requestToken) loading.value = false
    }
  }

  watch(query, term => {
    const trimmed = (term ?? '').trim()
    if (debounceTimer) clearTimeout(debounceTimer)

    if (trimmed.length < MIN_QUERY_LENGTH) {
      requestToken++ // cancel any in-flight result
      results.value = []
      loading.value = false
      error.value = null
      return
    }
    debounceTimer = setTimeout(() => run(trimmed), DEBOUNCE_MS)
  })

  function onSelect (city: CitySearchResult | null) {
    if (!city) return
    emit('select', city)
  }

  /** Called by the parent after a successful add, so the field is ready for the next one. */
  function reset () {
    selected.value = null
    query.value = ''
    results.value = []
  }

  defineExpose({ reset })

  onUnmounted(() => {
    if (debounceTimer) clearTimeout(debounceTimer)
  })
</script>
