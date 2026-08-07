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
      <v-card-title class="text-subtitle-1 pa-4 pb-0">{{ t('settings.aiHeading') }}</v-card-title>

      <v-card-text class="pa-4">
        <p class="text-caption text-medium-emphasis mb-3">{{ t('settings.aiNote') }}</p>

        <v-alert
          v-if="verifyError"
          class="mb-3"
          closable
          density="compact"
          type="error"
          variant="tonal"
          @click:close="verifyError = null"
        >
          {{ verifyError }}
        </v-alert>

        <div v-if="editingKey" class="d-flex flex-wrap ga-2 align-start">
          <v-text-field
            v-model="keyDraft"
            class="flex-grow-1"
            density="comfortable"
            :disabled="verifying"
            hide-details="auto"
            :label="t('settings.aiKeyLabel')"
            placeholder="sk-ant-..."
            prepend-inner-icon="mdi-key-outline"
            style="min-width: 240px"
            type="password"
            @keyup.enter="verifyAndSaveKey"
          />

          <v-btn
            color="primary"
            :disabled="!keyDraft.trim()"
            :loading="verifying"
            variant="flat"
            @click="verifyAndSaveKey"
          >
            {{ t('settings.aiKeyVerify') }}
          </v-btn>

          <v-btn
            v-if="userStore.hasAiApiKey"
            :disabled="verifying"
            variant="text"
            @click="cancelEdit"
          >
            {{ t('common.cancel') }}
          </v-btn>
        </div>

        <div v-else>
          <v-text-field
            append-inner-icon="mdi-check-circle"
            color="success"
            density="comfortable"
            disabled
            hide-details="auto"
            :label="t('settings.aiKeyLabel')"
            :model-value="maskedKey"
            prepend-inner-icon="mdi-key-outline"
          />

          <div class="d-flex align-center justify-space-between mt-2">
            <span class="text-caption text-medium-emphasis">{{ verifiedLabel }}</span>

            <v-btn size="small" variant="text" @click="changeKey">
              {{ t('settings.aiKeyChange') }}
            </v-btn>
          </div>
        </div>
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
  import { computed, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import LiveStatusChip from '@/components/LiveStatusChip.vue'
  import { ApiError, useApi } from '@/composables/useApi'
  import { useAppContext } from '@/composables/useAppContext'
  import { useLiveUpdates } from '@/composables/useLiveUpdates'
  import { SUPPORTED_LOCALES } from '@/plugins/i18n'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'
  import { useUserStore } from '@/stores/useUserStore'

  const { t, locale } = useI18n()
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

  // Starts in edit mode when no key is saved yet; otherwise the field opens locked,
  // showing what was last confirmed against Anthropic.
  const keyDraft = ref('')
  const editingKey = ref(!userStore.hasAiApiKey)
  const verifying = ref(false)
  const verifyError = ref<string | null>(null)

  const maskedKey = computed(() => {
    const key = userStore.aiApiKey
    return key.length > 4 ? `••••••••${key.slice(-4)}` : '••••••••'
  })

  const verifiedLabel = computed(() => {
    if (!userStore.aiApiKeyValidatedAt) {
      return ''
    }
    return t('settings.aiKeyVerified', { time: formatRelativeTime(userStore.aiApiKeyValidatedAt, locale.value) })
  })

  function formatRelativeTime (iso: string, localeCode: string): string {
    const diffMs = Date.parse(iso) - Date.now()
    const rtf = new Intl.RelativeTimeFormat(localeCode, { numeric: 'auto' })
    const diffMinutes = Math.round(diffMs / 60_000)
    if (Math.abs(diffMinutes) < 60) {
      return rtf.format(diffMinutes, 'minute')
    }
    const diffHours = Math.round(diffMs / 3_600_000)
    if (Math.abs(diffHours) < 24) {
      return rtf.format(diffHours, 'hour')
    }
    return rtf.format(Math.round(diffMs / 86_400_000), 'day')
  }

  async function verifyAndSaveKey () {
    const key = keyDraft.value.trim()
    if (!key) {
      return
    }

    verifying.value = true
    verifyError.value = null

    try {
      const { validatedAt } = await useApi().validateApiKey(key)
      userStore.setAiApiKey(key, validatedAt)
      keyDraft.value = ''
      editingKey.value = false
    } catch (error) {
      if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
        verifyError.value = t('settings.aiKeyInvalid')
      } else if (error instanceof ApiError && error.status === 0) {
        verifyError.value = t('settings.aiKeyUnreachable')
      } else {
        verifyError.value = t('settings.aiKeyCheckFailed')
      }
    } finally {
      verifying.value = false
    }
  }

  function changeKey () {
    keyDraft.value = ''
    verifyError.value = null
    editingKey.value = true
  }

  function cancelEdit () {
    keyDraft.value = ''
    verifyError.value = null
    editingKey.value = false
  }

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
