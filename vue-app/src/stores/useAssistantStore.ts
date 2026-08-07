import type { ChatMessage, PendingDeleteConfirmation } from '@shared/types'
import { defineStore } from 'pinia'
import { type ApiFailure, classifyError, useApi } from '@/composables/useApi'
import { useUserStore } from '@/stores/useUserStore'

/**
 * Parses a `text/event-stream` body into `(event, data)` pairs. Not `EventSource` -
 * it can't send a POST body, and this endpoint takes the conversation id/message as JSON.
 * Frames are `event:`/`data:` lines separated by a blank line; a data value spanning
 * multiple lines arrives as repeated `data:` lines within one frame (SSE spec), so those
 * are rejoined with `\n` rather than only reading the first one.
 */
async function readSse (
  response: Response,
  onEvent: (event: string, data: string) => void,
): Promise<void> {
  const reader = response.body?.getReader()
  if (!reader) {
    return
  }

  const decoder = new TextDecoder()
  let buffer = ''

  for (;;) {
    const { done, value } = await reader.read()
    if (done) {
      break
    }
    buffer += decoder.decode(value, { stream: true })

    let sepIndex: number
    while ((sepIndex = buffer.indexOf('\n\n')) !== -1) {
      const frame = buffer.slice(0, sepIndex)
      buffer = buffer.slice(sepIndex + 2)

      let event = 'message'
      const dataLines: string[] = []
      for (const line of frame.split('\n')) {
        if (line.startsWith('event:')) {
          event = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).trim())
        }
      }
      if (dataLines.length > 0) {
        onEvent(event, dataLines.join('\n'))
      }
    }
  }
}

export const useAssistantStore = defineStore('assistantStore', {
  state: () => ({
    // Not persisted - a reload starts a fresh conversation, and the backend's
    // ChatMemory is in-process only anyway, so an old id would just 404-equivalent.
    conversationId: crypto.randomUUID(),
    messages: [] as ChatMessage[],
    isOpen: false,
    sending: false,
    error: null as ApiFailure | null,
  }),

  actions: {
    toggle () {
      this.isOpen = !this.isOpen
    },

    async sendMessage (text: string) {
      const message = text.trim()
      // AiAssistantPanel hides the input entirely without a key; this is a backstop,
      // not the primary gate, so it fails quietly rather than surfacing an error.
      const apiKey = useUserStore().aiApiKey.trim()
      if (!message || !apiKey || this.sending) {
        return
      }

      this.error = null
      this.messages.push({ role: 'user', content: message, pendingConfirms: [] })
      const assistantMessage: ChatMessage = { role: 'assistant', content: '', pendingConfirms: [] }
      this.messages.push(assistantMessage)
      this.sending = true

      try {
        const response = await useApi().streamChat({
          conversationId: this.conversationId,
          message,
          apiKey,
        })
        await readSse(response, (event, data) => {
          switch (event) {
            case 'token': {
              assistantMessage.content += data

              break
            }
            case 'confirm-delete': {
              assistantMessage.pendingConfirms.push(JSON.parse(data) as PendingDeleteConfirmation)

              break
            }
            case 'error': {
              this.error = { kind: 'other', detail: data }

              break
            }
          // No default
          }
        })
      } catch (error) {
        this.messages.splice(this.messages.indexOf(assistantMessage), 1)
        this.error = classifyError(error)
      } finally {
        this.sending = false
      }
    },

    /** The AI only ever proposes a removal - confirming re-enters the normal REST path. */
    async confirmDelete (message: ChatMessage, pending: PendingDeleteConfirmation) {
      try {
        await useApi().deleteDestination(pending.id)
        message.pendingConfirms = message.pendingConfirms.filter(p => p.id !== pending.id)
      } catch (error) {
        this.error = classifyError(error)
      }
    },

    dismissConfirm (message: ChatMessage, pending: PendingDeleteConfirmation) {
      message.pendingConfirms = message.pendingConfirms.filter(p => p.id !== pending.id)
    },

    clearError () {
      this.error = null
    },
  },
})
