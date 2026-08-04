<template>
  <div class="pa-6 pa-sm-8" style="max-width: 760px">
    <div class="text-h6 mb-1">{{ t('settings.heading') }}</div>
    <p class="text-body-2 text-medium-emphasis mb-6">{{ t('settings.intro') }}</p>

    <v-card class="mb-6" elevation="1">
      <v-card-title class="text-subtitle-1 pa-4 pb-0">{{ t('settings.identity') }}</v-card-title>

      <v-card-text class="pa-4">
        <p class="text-caption text-medium-emphasis mb-3">{{ t('settings.identityNote') }}</p>

        <v-text-field
          v-model="displayName"
          counter="100"
          density="comfortable"
          hide-details="auto"
          :label="t('wishlist.yourName')"
          maxlength="100"
          prepend-inner-icon="mdi-account-outline"
        />
      </v-card-text>
    </v-card>

    <v-card class="mb-6" elevation="1">
      <v-card-title class="text-subtitle-1 pa-4 pb-0">{{ t('settings.appearance') }}</v-card-title>

      <v-card-text class="pa-4">
        <v-switch
          color="primary"
          density="comfortable"
          hide-details
          :label="t('settings.darkMode')"
          :model-value="userStore.theme === 'dark'"
          @update:model-value="userStore.toggleTheme()"
        />

        <v-select
          v-model="selectedLocale"
          class="mt-2"
          density="comfortable"
          hide-details
          :items="localeItems"
          :label="t('common.language')"
          style="max-width: 260px"
        />
      </v-card-text>
    </v-card>

    <v-card class="mb-6" elevation="1">
      <v-card-title class="text-subtitle-1 pa-4 pb-0">{{ t('settings.backend') }}</v-card-title>

      <v-card-text class="pa-4">
        <v-alert
          v-if="context.overridden"
          class="mb-3"
          density="compact"
          type="info"
          variant="tonal"
        >
          {{ t('settings.overrideActive') }}
        </v-alert>

        <v-list class="py-0 bg-transparent" density="compact">
          <v-list-item class="px-0">
            <v-list-item-title class="text-caption text-medium-emphasis">
              {{ t('settings.restBase') }}
            </v-list-item-title>

            <v-list-item-subtitle>
              <code>{{ context.apiBase || t('settings.sameOrigin') }}</code>
            </v-list-item-subtitle>
          </v-list-item>

          <v-list-item class="px-0">
            <v-list-item-title class="text-caption text-medium-emphasis">
              {{ t('settings.wsUrl') }}
            </v-list-item-title>

            <v-list-item-subtitle><code>{{ context.wsUrl }}</code></v-list-item-subtitle>
          </v-list-item>
        </v-list>

        <div class="d-flex flex-wrap align-center ga-3 mt-4">
          <LiveStatusChip />

          <v-btn prepend-icon="mdi-restart" size="small" variant="outlined" @click="reconnect">
            {{ t('live.reconnect') }}
          </v-btn>

          <v-btn
            :loading="destinationsStore.loading"
            prepend-icon="mdi-refresh"
            size="small"
            variant="text"
            @click="destinationsStore.load()"
          >
            {{ t('settings.reloadData') }}
          </v-btn>
        </div>

        <p class="text-caption text-medium-emphasis mt-4 mb-0">{{ t('settings.overrideHint') }}</p>
      </v-card-text>
    </v-card>

    <v-card elevation="1">
      <v-card-title class="text-subtitle-1 pa-4 pb-0">{{ t('settings.endpoints') }}</v-card-title>

      <v-card-text class="pa-4">
        <v-table density="compact">
          <thead>
            <tr>
              <th class="text-left">{{ t('settings.endpoint') }}</th>
              <th class="text-left">{{ t('settings.usedBy') }}</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="endpoint in ENDPOINTS" :key="endpoint.path">
              <td><code class="text-caption">{{ endpoint.path }}</code></td>
              <td class="text-caption text-medium-emphasis">{{ t(`nav.${endpoint.page}`) }}</td>
            </tr>
          </tbody>
        </v-table>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import LiveStatusChip from '@/components/LiveStatusChip.vue'
  import { useAppContext } from '@/composables/useAppContext'
  import { useLiveUpdates } from '@/composables/useLiveUpdates'
  import { SUPPORTED_LOCALES } from '@/plugins/i18n'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'
  import { useUserStore } from '@/stores/useUserStore'

  const { t } = useI18n()
  const context = useAppContext()
  const { reconnect } = useLiveUpdates()
  const userStore = useUserStore()
  const destinationsStore = useDestinationsStore()

  const displayName = computed({
    get: () => userStore.displayName,
    set: value => userStore.setDisplayName(value),
  })

  const selectedLocale = computed({
    get: () => userStore.locale,
    set: value => userStore.setLocale(value),
  })

  const localeItems = SUPPORTED_LOCALES.map(l => ({ title: `${l.flag}  ${l.label}`, value: l.code }))

  const ENDPOINTS = [
    { path: 'GET  /api/destinations', page: 'wishlist' },
    { path: 'POST /api/destinations', page: 'wishlist' },
    { path: 'GET  /api/cities/search?q=', page: 'explore' },
    { path: 'GET  /api/weather/city/{city}', page: 'weather' },
    { path: 'GET  /api/countries/{name}', page: 'countries' },
    { path: 'GET  /api/iss', page: 'iss' },
    { path: 'WS   /ws/destinations', page: 'live' },
  ]
</script>
