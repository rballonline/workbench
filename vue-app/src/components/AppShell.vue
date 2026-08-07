<template>
  <v-app :theme="userStore.theme">
    <AppSidebar />

    <v-app-bar border="b" density="comfortable" flat>
      <v-app-bar-title class="text-subtitle-1">{{
        t(`nav.${String(route.name)}`)
      }}</v-app-bar-title>

      <template #append>
        <LiveStatusChip class="mr-2" />
        <LocaleSelector class="mr-1" />

        <v-btn
          :aria-label="t('assistant.title')"
          density="comfortable"
          icon="mdi-robot-outline"
          variant="text"
          @click="assistantStore.toggle()"
        />

        <v-btn
          :aria-label="t('common.toggleTheme')"
          density="comfortable"
          :icon="
            userStore.theme === 'light'
              ? 'mdi-weather-night'
              : 'mdi-weather-sunny'
          "
          variant="text"
          @click="userStore.toggleTheme()"
        />
      </template>
    </v-app-bar>

    <v-navigation-drawer
      v-model="assistantStore.isOpen"
      location="right"
      temporary
      width="400"
    >
      <AiAssistantPanel />
    </v-navigation-drawer>

    <v-main class="bg-surface-variant-subtle">
      <router-view v-slot="{ Component }">
        <Suspense>
          <component :is="Component" />

          <template #fallback>
            <div
              class="d-flex justify-center align-center"
              style="min-height: 200px"
            >
              <v-progress-circular color="primary" indeterminate />
            </div>
          </template>
        </Suspense>
      </router-view>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
  import { isDestinationEvent } from '@shared/types'
  import { onMounted, onUnmounted, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useRoute } from 'vue-router'
  import { provideAppContext } from '@/composables/useAppContext'
  import { useLiveUpdates } from '@/composables/useLiveUpdates'
  import { useAssistantStore } from '@/stores/useAssistantStore'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'
  import { useIssStore } from '@/stores/useIssStore'
  import { useUserStore } from '@/stores/useUserStore'
  import AiAssistantPanel from './AiAssistantPanel.vue'
  import AppSidebar from './AppSidebar.vue'
  import LiveStatusChip from './LiveStatusChip.vue'
  import LocaleSelector from './LocaleSelector.vue'

  provideAppContext()

  const { t, locale } = useI18n()
  const route = useRoute()

  const userStore = useUserStore()
  const destinationsStore = useDestinationsStore()
  const issStore = useIssStore()
  const assistantStore = useAssistantStore()

  // The locale the user picked last session; applied once Pinia has rehydrated.
  locale.value = userStore.locale
  watch(
    () => userStore.locale,
    next => {
      locale.value = next
    },
  )

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

  // The socket itself is intentionally left open - it is a module-level singleton
  // shared across the app's lifetime, not owned by this component.
  onUnmounted(() => unsubscribe?.())
</script>

<style scoped>
.bg-surface-variant-subtle {
  background: rgba(var(--v-theme-on-surface), 0.03);
}
</style>
