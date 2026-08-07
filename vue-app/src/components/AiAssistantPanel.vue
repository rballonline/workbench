<template>
  <div class="d-flex flex-column fill-height">
    <div class="pa-4 pb-2">
      <div class="text-subtitle-2 font-weight-bold">{{ t('assistant.title') }}</div>
      <div class="text-caption text-medium-emphasis">{{ t('assistant.subtitle') }}</div>
    </div>

    <v-divider />

    <div v-if="!userStore.hasAiApiKey" class="flex-grow-1 d-flex flex-column align-center justify-center pa-6 text-center">
      <v-icon class="mb-4" color="grey-lighten-1" size="40">mdi-key-outline</v-icon>
      <div class="text-body-2 text-medium-emphasis mb-4">{{ t('assistant.noKey') }}</div>

      <v-btn color="primary" prepend-icon="mdi-cog-outline" variant="tonal" @click="goToSettings">
        {{ t('assistant.noKeyCta') }}
      </v-btn>
    </div>

    <div v-else ref="scrollEl" class="flex-grow-1 overflow-y-auto pa-4">
      <ApiErrorAlert :failure="assistantStore.error" @close="assistantStore.clearError()" />

      <div v-if="assistantStore.messages.length === 0" class="text-body-2 text-medium-emphasis pa-4 text-center">
        {{ t('assistant.empty') }}
      </div>

      <div
        v-for="(message, index) in assistantStore.messages"
        :key="index"
        class="d-flex mb-3"
        :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
      >
        <v-card
          :color="message.role === 'user' ? 'primary' : undefined"
          max-width="85%"
          rounded="lg"
          :variant="message.role === 'user' ? 'flat' : 'tonal'"
        >
          <v-card-text class="text-body-2 py-2 px-3" style="white-space: pre-wrap">
            <span v-if="message.content">{{ message.content }}</span>

            <v-progress-circular
              v-else-if="assistantStore.sending && isLastMessage(index)"
              color="primary"
              indeterminate
              size="16"
              width="2"
            />
          </v-card-text>

          <v-card-text
            v-for="pending in message.pendingConfirms"
            :key="pending.id"
            class="pt-0 pb-2 px-3"
          >
            <v-alert density="compact" type="warning" variant="tonal">
              <div class="text-body-2 mb-2">
                {{ t('assistant.confirmRemove', { city: pending.cityName }) }}
              </div>

              <div class="d-flex ga-2">
                <v-btn
                  color="warning"
                  size="small"
                  variant="elevated"
                  @click="assistantStore.confirmDelete(message, pending)"
                >{{ t('common.remove') }}</v-btn>

                <v-btn
                  size="small"
                  variant="text"
                  @click="assistantStore.dismissConfirm(message, pending)"
                >{{ t('common.cancel') }}</v-btn>
              </div>
            </v-alert>
          </v-card-text>
        </v-card>
      </div>
    </div>

    <template v-if="userStore.hasAiApiKey">
      <v-divider />

      <form class="pa-3 d-flex ga-2" @submit.prevent="submit">
        <v-text-field
          v-model="draft"
          density="comfortable"
          :disabled="assistantStore.sending"
          hide-details
          :placeholder="t('assistant.placeholder')"
          variant="outlined"
        />

        <v-btn
          color="primary"
          :disabled="assistantStore.sending || !draft.trim()"
          icon="mdi-send"
          type="submit"
          variant="flat"
        />
      </form>
    </template>
  </div>
</template>

<script setup lang="ts">
  import { nextTick, ref, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useRouter } from 'vue-router'
  import { useAssistantStore } from '@/stores/useAssistantStore'
  import { useUserStore } from '@/stores/useUserStore'
  import ApiErrorAlert from './ApiErrorAlert.vue'

  const { t } = useI18n()
  const router = useRouter()
  const assistantStore = useAssistantStore()
  const userStore = useUserStore()

  const draft = ref('')
  const scrollEl = ref<HTMLElement | null>(null)

  function isLastMessage (index: number): boolean {
    return index === assistantStore.messages.length - 1
  }

  function submit () {
    const text = draft.value
    draft.value = ''
    assistantStore.sendMessage(text)
  }

  function goToSettings () {
    assistantStore.isOpen = false
    router.push({ name: 'settings' })
  }

  // Streamed tokens mutate the last message's `content` in place rather than pushing
  // new array entries, so a deep watch is needed to notice growth and autoscroll.
  watch(
    () => assistantStore.messages,
    async () => {
      await nextTick()
      scrollEl.value?.scrollTo({ top: scrollEl.value.scrollHeight })
    },
    { deep: true },
  )
</script>
