<template>
  <v-alert
    v-if="failure"
    class="mb-6"
    closable
    :type="failure.kind === 'unreachable' ? 'warning' : 'error'"
    variant="tonal"
    @click:close="$emit('close')"
  >
    <div class="text-body-2">{{ headline }}</div>
    <div v-if="showDetail" class="text-caption text-medium-emphasis mt-1">{{ failure.detail }}</div>
  </v-alert>
</template>

<script setup lang="ts">
  import type { ApiFailure } from '@/composables/useApi'
  import { computed } from 'vue'
  import { useI18n } from 'vue-i18n'

  const props = defineProps<{
    failure: ApiFailure | null
    /** Page-specific wording for a 404, e.g. "No weather found for Tokyo". */
    notFound?: string
  }>()

  defineEmits<{ close: [] }>()

  const { t } = useI18n()

  const headline = computed(() => {
    if (!props.failure) return ''
    if (props.failure.kind === 'notFound' && props.notFound) return props.notFound
    return t(`common.errors.${props.failure.kind}`)
  })

  // For validation and not-found the detail is the message; repeating it reads as a
  // stutter. For the other kinds it is the only technical clue available.
  const showDetail = computed(
    () => props.failure?.kind === 'unreachable' || props.failure?.kind === 'other',
  )
</script>
