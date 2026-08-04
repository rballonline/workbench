<template>
  <v-app :theme="userStore.theme">
    <AppSidebar :current-page="currentPage" @navigate="navigate" />

    <v-app-bar border="b" density="comfortable" flat>
      <v-app-bar-title class="text-subtitle-1">{{ t(`nav.${currentPage}`) }}</v-app-bar-title>

      <template #append>
        <LiveStatusChip class="mr-2" />
        <LocaleSelector class="mr-1" />

        <v-btn
          :aria-label="t('common.toggleTheme')"
          density="comfortable"
          :icon="userStore.theme === 'light' ? 'mdi-weather-night' : 'mdi-weather-sunny'"
          variant="text"
          @click="userStore.toggleTheme()"
        />
      </template>
    </v-app-bar>

    <v-main class="bg-surface-variant-subtle">
      <Suspense>
        <component :is="currentPageComponent" @navigate="navigate" />

        <template #fallback>
          <div class="d-flex justify-center align-center" style="min-height: 200px">
            <v-progress-circular color="primary" indeterminate />
          </div>
        </template>
      </Suspense>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
  import { isDestinationEvent } from '@shared/types'
  import { computed, defineAsyncComponent, onMounted, onUnmounted, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { provideAppContext } from '@/composables/useAppContext'
  import { DEFAULT_PAGE, useAppNav } from '@/composables/useAppNav'
  import { useLiveUpdates } from '@/composables/useLiveUpdates'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'
  import { useIssStore } from '@/stores/useIssStore'
  import { useUserStore } from '@/stores/useUserStore'
  import AppSidebar from './AppSidebar.vue'
  import LiveStatusChip from './LiveStatusChip.vue'
  import LocaleSelector from './LocaleSelector.vue'

  provideAppContext()

  const { t, locale } = useI18n()
  const { currentPage, navigate } = useAppNav()

  const userStore = useUserStore()
  const destinationsStore = useDestinationsStore()
  const issStore = useIssStore()

  const PAGE_COMPONENTS = {
    welcome: defineAsyncComponent(() => import('./pages/WelcomePage.vue')),
    wishlist: defineAsyncComponent(() => import('./pages/WishlistPage.vue')),
    explore: defineAsyncComponent(() => import('./pages/ExplorePage.vue')),
    weather: defineAsyncComponent(() => import('./pages/WeatherPage.vue')),
    countries: defineAsyncComponent(() => import('./pages/CountriesPage.vue')),
    iss: defineAsyncComponent(() => import('./pages/IssTrackerPage.vue')),
    live: defineAsyncComponent(() => import('./pages/LiveFeedPage.vue')),
    settings: defineAsyncComponent(() => import('./pages/SettingsPage.vue')),
  } as const

  const currentPageComponent = computed(
    () => PAGE_COMPONENTS[currentPage.value as keyof typeof PAGE_COMPONENTS] ?? PAGE_COMPONENTS[DEFAULT_PAGE],
  )

  // The locale the user picked last session; applied once Pinia has rehydrated.
  locale.value = userStore.locale
  watch(() => userStore.locale, next => {
    locale.value = next
  })

  // The shell owns the single socket and fans frames out to the stores, so no page
  // has to care whether the connection already exists.
  const { connect, onMessage } = useLiveUpdates()
  let unsubscribe: (() => void) | null = null

  onMounted(() => {
    unsubscribe = onMessage(message => {
      if (isDestinationEvent(message)) destinationsStore.applyEvent(message)
      else issStore.record(message)
    })
    connect()
    destinationsStore.load()
  })

  // The socket itself is intentionally left open — it is a module-level singleton
  // shared across the app's lifetime, not owned by this component.
  onUnmounted(() => unsubscribe?.())
</script>

<style scoped>
  .bg-surface-variant-subtle {
    background: rgba(var(--v-theme-on-surface), 0.03);
  }
</style>
