import type {
  AddDestinationRequest,
  Destination,
  DestinationEvent,
} from '@shared/types'
import { defineStore } from 'pinia'
import { type ApiFailure, classifyError, useApi } from '@/composables/useApi'

export const useDestinationsStore = defineStore('destinationsStore', {
  state: () => ({
    items: [] as Destination[],
    loading: false,
    saving: false,
    /** Classified rather than pre-worded, so the page can translate it. */
    error: null as ApiFailure | null,
    /** Ids that arrived over the WebSocket rather than from this browser's own POST. */
    remoteIds: [] as number[],
  }),

  getters: {
    count: state => state.items.length,

    /** Newest first - the backend returns insertion order. */
    sorted: state =>
      state.items.toSorted((a, b) => (a.createdAt < b.createdAt ? 1 : -1)),

    /** `{ [region]: Destination[] }`, for the breakdown on the home page. */
    byRegion: state => {
      const groups: Record<string, Destination[]> = {}
      for (const d of state.items) {
        const region = d.country?.region ?? 'Unknown';
        (groups[region] ??= []).push(d)
      }
      return groups
    },

    contributors: state => {
      const names = new Set<string>()
      for (const d of state.items) {
        if (d.addedBy) {
          names.add(d.addedBy)
        }
      }
      return [...names].toSorted()
    },
  },

  actions: {
    async load () {
      this.loading = true
      this.error = null
      try {
        this.items = await useApi().listDestinations()
      } catch (error) {
        this.error = classifyError(error)
      } finally {
        this.loading = false
      }
    },

    async add (request: AddDestinationRequest): Promise<Destination | null> {
      this.saving = true
      this.error = null
      try {
        const created = await useApi().addDestination(request)
        this.upsert(created)
        return created
      } catch (error) {
        this.error = classifyError(error)
        return null
      } finally {
        this.saving = false
      }
    },

    async remove (id: number) {
      this.error = null
      try {
        await useApi().deleteDestination(id)
        this.dropById(id)
      } catch (error) {
        this.error = classifyError(error)
      }
    },

    /**
		 * Fold a WebSocket event into the list. The event carries the raw entity, which
		 * has `countryCode` but no nested `country`, so a create is refetched by id to
		 * pick up the joined country row the list renders.
		 */
    async applyEvent (event: DestinationEvent) {
      const { id } = event.destination

      if (event.action === 'DELETED') {
        this.dropById(id)
        return
      }

      // Our own POST already upserted the enriched response; skip the round trip.
      if (event.action === 'CREATED' && this.items.some(d => d.id === id)) {
        return
      }

      try {
        this.upsert(await useApi().getDestination(id))
        if (!this.remoteIds.includes(id)) {
          this.remoteIds.push(id)
        }
      } catch {
        // A refetch failure only means this client stays slightly stale; the next
        // full load reconciles. Not worth surfacing as a page-level error.
      }
    },

    upsert (destination: Destination) {
      const index = this.items.findIndex(d => d.id === destination.id)
      if (index === -1) {
        this.items.push(destination)
      } else {
        this.items[index] = destination
      }
    },

    dropById (id: number) {
      this.items = this.items.filter(d => d.id !== id)
      this.remoteIds = this.remoteIds.filter(remoteId => remoteId !== id)
    },

    clearError () {
      this.error = null
    },
  },
})
