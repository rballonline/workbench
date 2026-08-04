import { isDestinationEvent, type LiveMessage } from '@shared/types'
import { readonly, ref, shallowRef } from 'vue'
import { getAppContext } from './useAppContext'

export type LiveStatus = 'idle' | 'connecting' | 'open' | 'closed'

type Listener = (msg: LiveMessage) => void

/** One entry in the raw feed shown on the Live page. */
export interface LiveLogEntry {
  id: number
  receivedAt: number
  message: LiveMessage
}

const MAX_LOG_ENTRIES = 200
const MAX_BACKOFF_MS = 15_000

// Singleton at module scope: `/ws/destinations` broadcasts the same merged stream to
// every session, so a second socket would only duplicate work. Pages come and go;
// the connection outlives all of them.
const status = ref<LiveStatus>('idle')
const lastError = ref<string | null>(null)
const log = shallowRef<LiveLogEntry[]>([])
const listeners = new Set<Listener>()

let socket: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let attempt = 0
let nextEntryId = 1
let manuallyClosed = false

function scheduleReconnect () {
  if (manuallyClosed || reconnectTimer) {
    return
  }
  // Exponential backoff, capped — the backend restarts often in dev and we don't
  // want a tight reconnect loop hammering it while it boots.
  const delay = Math.min(500 * 2 ** attempt, MAX_BACKOFF_MS)
  attempt += 1
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    open()
  }, delay)
}

function handleMessage (raw: string) {
  let message: LiveMessage
  try {
    message = JSON.parse(raw) as LiveMessage
  } catch {
    lastError.value = 'Received a malformed frame'
    return
  }

  log.value = [
    { id: nextEntryId++, receivedAt: Date.now(), message },
    ...log.value,
  ].slice(0, MAX_LOG_ENTRIES)

  for (const listener of listeners) {
    listener(message)
  }
}

function open () {
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
    return
  }

  manuallyClosed = false
  status.value = 'connecting'

  const ws = new WebSocket(getAppContext().wsUrl)
  socket = ws

  ws.addEventListener('open', () => {
    attempt = 0
    lastError.value = null
    status.value = 'open'
  })

  ws.addEventListener('message', event => {
    if (typeof event.data === 'string') {
      handleMessage(event.data)
    }
  })

  ws.addEventListener('error', () => {
    // The browser deliberately withholds the reason; `close` follows with the detail
    // we're allowed to see, so only note that something went wrong here.
    lastError.value = 'WebSocket error'
  })

  ws.addEventListener('close', event => {
    if (socket === ws) {
      socket = null
    }
    status.value = 'closed'
    if (!manuallyClosed) {
      lastError.value = event.reason || lastError.value || 'Connection closed'
      scheduleReconnect()
    }
  })
}

export function useLiveUpdates () {
  function connect () {
    open()
  }

  function disconnect () {
    manuallyClosed = true
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    socket?.close()
    socket = null
    status.value = 'closed'
  }

  function reconnect () {
    disconnect()
    attempt = 0
    open()
  }

  /** Register a handler for every frame. Returns the unsubscribe function. */
  function onMessage (listener: Listener): () => void {
    listeners.add(listener)
    return () => listeners.delete(listener)
  }

  function clearLog () {
    log.value = []
  }

  return {
    status: readonly(status),
    lastError: readonly(lastError),
    log: readonly(log),
    connect,
    disconnect,
    reconnect,
    onMessage,
    clearLog,
    isDestinationEvent,
  }
}
