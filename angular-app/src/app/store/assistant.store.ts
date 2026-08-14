import { Injectable, inject, signal, computed } from '@angular/core';
import type { ChatMessage, PendingDeleteConfirmation, ChatRequest } from '@shared/types';
import { ApiService, type ApiFailure } from '../core/services/api.service';
import { UserStore } from './user.store';

/**
 * Parses a `text/event-stream` body into `(event, data)` pairs. Not `EventSource` -
 * it can't send a POST body, and this endpoint takes the conversation id/message as JSON.
 * Frames are `event:`/`data:` lines separated by a blank line; a data value spanning
 * multiple lines arrives as repeated `data:` lines within one frame (SSE spec), so those
 * are rejoined with `\n` rather than only reading the first one.
 */
async function readSse(
  response: Response,
  onEvent: (event: string, data: string) => void
): Promise<void> {
  const reader = response.body?.getReader();
  if (!reader) {
    return;
  }

  const decoder = new TextDecoder();
  let buffer = '';

  for (;;) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });

    let sepIndex: number;
    while ((sepIndex = buffer.indexOf('\n\n')) !== -1) {
      const frame = buffer.slice(0, sepIndex);
      buffer = buffer.slice(sepIndex + 2);

      let event = 'message';
      const dataLines: string[] = [];
      for (const line of frame.split('\n')) {
        if (line.startsWith('event:')) {
          event = line.slice(6).trim();
        } else if (line.startsWith('data:')) {
          // Per the SSE spec, only a single leading space after the colon is stripped -
          // the rest of the payload (including meaningful leading/trailing spaces within
          // a streamed token) must survive, or words run together on the client.
          const value = line.slice(5);
          dataLines.push(value.startsWith(' ') ? value.slice(1) : value);
        }
      }
      if (dataLines.length > 0) {
        onEvent(event, dataLines.join('\n'));
      }
    }
  }
}

@Injectable({ providedIn: 'root' })
export class AssistantStore {
  private readonly apiService = inject(ApiService);
  private readonly userStore = inject(UserStore);

  // Not persisted - a reload starts a fresh conversation, and the backend's
  // ChatMemory is in-process only anyway, so an old id would just 404-equivalent.
  private readonly _conversationId = signal<string>(crypto.randomUUID());
  private readonly _messages = signal<ChatMessage[]>([]);
  private readonly _isOpen = signal(false);
  private readonly _sending = signal(false);
  private readonly _error = signal<ApiFailure | null>(null);

  readonly conversationId = this._conversationId.asReadonly();
  readonly messages = this._messages.asReadonly();
  readonly isOpen = this._isOpen.asReadonly();
  readonly sending = this._sending.asReadonly();
  readonly error = this._error.asReadonly();

  readonly hasMessages = computed(() => this._messages().length > 0);

  toggle(): void {
    this._isOpen.update(v => !v);
  }

  async sendMessage(text: string): Promise<void> {
    const message = text.trim();
    // AiAssistantPanel hides the input entirely without a key; this is a backstop,
    // not the primary gate, so it fails quietly rather than surfacing an error.
    const apiKey = this.userStore.aiApiKey().trim();
    if (!message || !apiKey || this._sending()) {
      return;
    }

    this._error.set(null);
    this._messages.update(msgs => [...msgs, { role: 'user', content: message, pendingConfirms: [] }]);
    const assistantMessage: ChatMessage = { role: 'assistant', content: '', pendingConfirms: [] };
    this._messages.update(msgs => [...msgs, assistantMessage]);
    this._sending.set(true);

    const request: ChatRequest = {
      conversationId: this._conversationId(),
      message,
      apiKey
    };

    try {
      const response = await this.apiService.streamChat(request);
      await readSse(response, (event, data) => {
        switch (event) {
          case 'token': {
            assistantMessage.content += data;
            this._messages.update(msgs => [...msgs]);
            break;
          }
          case 'confirm-delete': {
            assistantMessage.pendingConfirms = [
              ...assistantMessage.pendingConfirms,
              JSON.parse(data) as PendingDeleteConfirmation
            ];
            this._messages.update(msgs => [...msgs]);
            break;
          }
          case 'error': {
            this._error.set({ kind: 'other', detail: data });
            break;
          }
        }
      });
    } catch (error) {
      this._messages.update(msgs => msgs.filter(m => m !== assistantMessage));
      this._error.set(this.classifyError(error));
    } finally {
      this._sending.set(false);
    }
  }

  /** The AI only ever proposes a removal - confirming re-enters the normal REST path. */
  async confirmDelete(message: ChatMessage, pending: PendingDeleteConfirmation): Promise<void> {
    try {
      await this.apiService.deleteDestination(pending.id);
      message.pendingConfirms = message.pendingConfirms.filter(p => p.id !== pending.id);
      this._messages.update(msgs => [...msgs]);
    } catch (error) {
      this._error.set(this.classifyError(error));
    }
  }

  dismissConfirm(message: ChatMessage, pending: PendingDeleteConfirmation): void {
    message.pendingConfirms = message.pendingConfirms.filter(p => p.id !== pending.id);
    this._messages.update(msgs => [...msgs]);
  }

  clearError(): void {
    this._error.set(null);
  }

  private classifyError(error: unknown): ApiFailure {
    return this.apiService.classifyError(error);
  }
}
