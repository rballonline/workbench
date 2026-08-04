/**
 * Props for `WorldMap.vue`. Kept in a sibling module because `<script setup>` cannot
 * export declarations, and both the ISS and wishlist pages build these shapes.
 */

export interface MapPoint {
  latitude: number
  longitude: number
}

export interface MapMarker {
  lat: number
  lon: number
  label?: string
  color?: string
  size?: number
  /** Draws a soft halo — used for the single "live" marker. */
  pulse?: boolean
}

export interface MapTrail {
  /** Pre-split so an antimeridian crossing doesn't draw back across the map. */
  segments: MapPoint[][]
  color?: string
}
