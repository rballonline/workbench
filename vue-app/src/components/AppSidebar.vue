<template>
  <v-navigation-drawer permanent width="240">
    <div class="pa-4 pb-2">
      <div class="text-subtitle-2 font-weight-bold">{{ t('app.title') }}</div>
      <div class="text-caption text-medium-emphasis">{{ t('app.subtitle') }}</div>
    </div>

    <v-divider />

    <v-list class="py-2" density="compact" nav>
      <template v-for="section in NAV_SECTIONS" :key="section.header ?? '_top'">
        <v-list-subheader v-if="section.header" class="text-uppercase">
          {{ t(`nav.sections.${section.header}`) }}
        </v-list-subheader>

        <v-list-item
          v-for="item in section.items"
          :key="item.page"
          :active="currentPage === item.page"
          color="primary"
          :prepend-icon="item.icon"
          rounded="lg"
          :title="t(`nav.${item.page}`)"
          @click="$emit('navigate', item.page)"
        >
          <template v-if="item.page === 'wishlist' && destinationsStore.count > 0" #append>
            <v-chip size="x-small" variant="tonal">{{ destinationsStore.count }}</v-chip>
          </template>
        </v-list-item>

        <v-divider v-if="section.dividerAfter" class="my-2" />
      </template>
    </v-list>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { useDestinationsStore } from '@/stores/useDestinationsStore'

  defineProps<{ currentPage: string }>()
  defineEmits<{ navigate: [page: string] }>()

  const { t } = useI18n()
  const destinationsStore = useDestinationsStore()

  const NAV_SECTIONS = [
    {
      dividerAfter: true,
      items: [
        { page: 'home', icon: 'mdi-home-outline' },
      ],
    },
    {
      header: 'plan',
      dividerAfter: true,
      items: [
        { page: 'wishlist', icon: 'mdi-map-marker-multiple-outline' },
        { page: 'explore', icon: 'mdi-magnify' },
      ],
    },
    {
      header: 'lookup',
      dividerAfter: true,
      items: [
        { page: 'weather', icon: 'mdi-weather-partly-cloudy' },
        { page: 'countries', icon: 'mdi-flag-outline' },
      ],
    },
    {
      header: 'live',
      dividerAfter: true,
      items: [
        { page: 'iss', icon: 'mdi-satellite-variant' },
        { page: 'live', icon: 'mdi-pulse' },
      ],
    },
    {
      items: [
        { page: 'settings', icon: 'mdi-cog-outline' },
      ],
    },
  ]
</script>
