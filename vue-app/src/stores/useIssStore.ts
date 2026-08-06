import type { IssPosition } from '@shared/types'
import { defineStore } from 'pinia'
import { type ApiFailure, classifyError, useApi } from '@/composables/useApi'

/** Roughly 25 minutes of trail at the backend's 5-second poll interval. */
const MAX_TRAIL_POINTS = 300

export const useIssStore = defineStore('issStore', {
  state: () => ({
    position: null as IssPosition | null,
    /** Oldest first, so it can be drawn straight into an SVG polyline. */
    trail: [] as IssPosition[],
    loading: false,
    /** Classified rather than pre-worded, so the page can translate it. */
    error: null as ApiFailure | null,
  }),

  getters: {
    /**
     * Trail split into runs that don't cross the antimeridian, so an equirectangular
     * plot doesn't draw a line straight back across the map on each wrap.
     */
    trailSegments: state => {
      const segments: IssPosition[][] = []
      let current: IssPosition[] = []
      for (const point of state.trail) {
        const previous = current.at(-1)
        if (previous && Math.abs(point.longitude - previous.longitude) > 180) {
          segments.push(current)
          current = []
        }
        current.push(point)
      }
      if (current.length > 0) {
        segments.push(current)
      }
      return segments
    },
  },

  actions: {
    /** One-shot REST read, used to paint something before the first WebSocket tick. */
    async fetchOnce () {
      this.loading = true
      this.error = null
      try {
        this.record(await useApi().getIssPosition())
      } catch (error) {
        this.error = classifyError(error)
      } finally {
        this.loading = false
      }
    },

    record (position: IssPosition) {
      // The socket replays the same sample if a poll lands between ticks.
      if (this.position?.timestamp === position.timestamp) {
        return
      }
      this.position = position
      this.trail.push(position)
      if (this.trail.length > MAX_TRAIL_POINTS) {
        this.trail.splice(0, this.trail.length - MAX_TRAIL_POINTS)
      }
    },

    clearTrail () {
      this.trail = this.position ? [this.position] : []
    },
  },
})
