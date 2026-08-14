import { Injectable, signal, OnDestroy, inject } from '@angular/core';
import type { LiveMessage } from '@shared/types';
import { AppContextService } from './app-context.service';

export type LiveStatus = 'idle' | 'connecting' | 'open' | 'closed';

export interface LiveLogEntry {
  id: number;
  receivedAt: number;
  message: LiveMessage;
}

@Injectable({ providedIn: 'root' })
export class LiveUpdatesService implements OnDestroy {
  private readonly appContext = inject(AppContextService);

  private readonly _status = signal<LiveStatus>('idle');
  private readonly _lastError = signal<string | null>(null);
  private readonly _log = signal<LiveLogEntry[]>([]);

  readonly status = this._status.asReadonly();
  readonly lastError = this._lastError.asReadonly();
  readonly log = this._log.asReadonly();

  private socket: WebSocket | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private attempt = 0;
  private nextEntryId = 1;
  private manuallyClosed = false;
  private listeners = new Set<(msg: LiveMessage) => void>();

  private readonly MAX_LOG_ENTRIES = 200;
  private readonly MAX_BACKOFF_MS = 15_000;

  connect(): void {
    this.open();
  }

  disconnect(): void {
    this.manuallyClosed = true;
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.socket?.readyState === WebSocket.OPEN) {
      this.socket.close();
    }
  }

  onMessage(listener: (msg: LiveMessage) => void): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  private open(): void {
    if (
      this.socket
      && (this.socket.readyState === WebSocket.OPEN
        || this.socket.readyState === WebSocket.CONNECTING)
    ) {
      return;
    }

    this.manuallyClosed = false;
    this._status.set('connecting');

    const ws = new WebSocket(this.appContext.wsUrl);
    this.socket = ws;

    ws.addEventListener('open', () => {
      this.attempt = 0;
      this._lastError.set(null);
      this._status.set('open');
    });

    ws.addEventListener('message', (event) => {
      if (typeof event.data === 'string') {
        this.handleMessage(event.data);
      }
    });

    ws.addEventListener('error', () => {
      this._lastError.set('WebSocket error');
    });

    ws.addEventListener('close', (event) => {
      if (this.socket === ws) {
        this.socket = null;
      }
      this._status.set('closed');
      if (!this.manuallyClosed) {
        this._lastError.set(event.reason || this._lastError() || 'Connection closed');
        this.scheduleReconnect();
      }
    });
  }

  private scheduleReconnect(): void {
    if (this.manuallyClosed || this.reconnectTimer) {
      return;
    }
    const delay = Math.min(500 * 2 ** this.attempt, this.MAX_BACKOFF_MS);
    this.attempt += 1;
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      this.open();
    }, delay);
  }

  private handleMessage(raw: string): void {
    let message: LiveMessage;
    try {
      message = JSON.parse(raw) as LiveMessage;
    } catch {
      this._lastError.set('Received a malformed frame');
      return;
    }

    this._log.update(log => [
      { id: this.nextEntryId++, receivedAt: Date.now(), message },
      ...log
    ].slice(0, this.MAX_LOG_ENTRIES));

    for (const listener of this.listeners) {
      listener(message);
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
