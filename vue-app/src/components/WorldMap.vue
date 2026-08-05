<template>
  <!--
    Equirectangular plate carrée: the viewBox is literally 360 x 180 degrees, so
    projecting is `x = lon + 180`, `y = 90 - lat` with no scaling maths anywhere else.
    Drawn as a graticule rather than a coastline outline to keep the component
    dependency-free - no map tiles, no bundled world geometry.
  -->
  <svg
    :aria-label="ariaLabel"
    class="world-map"
    preserveAspectRatio="xMidYMid meet"
    role="img"
    viewBox="0 0 360 180"
  >
    <rect
      class="ocean"
      height="180"
      width="360"
      x="0"
      y="0"
    />

    <g class="graticule">
      <line
        v-for="lon in MERIDIANS"
        :key="`m${lon}`"
        :x1="lon + 180"
        :x2="lon + 180"
        y1="0"
        y2="180"
      />

      <line
        v-for="lat in PARALLELS"
        :key="`p${lat}`"
        x1="0"
        x2="360"
        :y1="90 - lat"
        :y2="90 - lat"
      />
    </g>

    <g class="graticule-major">
      <line x1="0" x2="360" y1="90" y2="90" />
      <line x1="180" x2="180" y1="0" y2="180" />
    </g>

    <g class="graticule-tropic">
      <line x1="0" x2="360" :y1="90 - 23.44" :y2="90 - 23.44" />
      <line x1="0" x2="360" :y1="90 + 23.44" :y2="90 + 23.44" />
    </g>

    <g v-for="(trail, index) in trails" :key="`t${index}`">
      <polyline
        v-for="(segment, sIndex) in trail.segments"
        :key="sIndex"
        class="trail"
        :points="toPoints(segment)"
        :stroke="trail.color ?? 'rgb(var(--v-theme-secondary))'"
      />
    </g>

    <g v-for="(marker, index) in markers" :key="`k${index}`">
      <circle
        v-if="marker.pulse"
        class="marker-pulse"
        :cx="marker.lon + 180"
        :cy="90 - marker.lat"
        :fill="marker.color ?? 'rgb(var(--v-theme-primary))'"
        :r="(marker.size ?? 2.5) * 2.4"
      />

      <circle
        class="marker"
        :cx="marker.lon + 180"
        :cy="90 - marker.lat"
        :fill="marker.color ?? 'rgb(var(--v-theme-primary))'"
        :r="marker.size ?? 2.5"
      >
        <title v-if="marker.label">{{ marker.label }}</title>
      </circle>
    </g>
  </svg>
</template>

<script setup lang="ts">
  import type { MapMarker, MapPoint, MapTrail } from './WorldMap.types'

  withDefaults(
    defineProps<{
      markers?: MapMarker[]
      trails?: MapTrail[]
      ariaLabel?: string
    }>(),
    { markers: () => [], trails: () => [], ariaLabel: 'World map' },
  )

  const MERIDIANS = [-150, -120, -90, -60, -30, 30, 60, 90, 120, 150]
  const PARALLELS = [-60, -30, 30, 60]

  function toPoints (segment: MapPoint[]): string {
    return segment
      .map(p => `${p.longitude + 180},${90 - p.latitude}`)
      .join(' ')
  }
</script>

<style scoped>
.world-map {
	display: block;
	/* The viewBox is 360x180 degrees, so an unconstrained width makes the map half as
       tall as the page is wide. Cap it so it stays a map rather than a wall. */
	width: 100%;
	max-width: 880px;
	height: auto;
	margin-inline: auto;
	border-radius: 6px;
}

.ocean {
	fill: rgba(var(--v-theme-primary), 0.06);
}

.graticule line {
	stroke: rgba(var(--v-theme-on-surface), 0.12);
	stroke-width: 0.3;
}

.graticule-major line {
	stroke: rgba(var(--v-theme-on-surface), 0.28);
	stroke-width: 0.45;
}

.graticule-tropic line {
	stroke: rgba(var(--v-theme-on-surface), 0.14);
	stroke-width: 0.3;
	stroke-dasharray: 2 2;
}

.trail {
	fill: none;
	stroke-width: 0.9;
	stroke-linecap: round;
	stroke-linejoin: round;
	opacity: 0.75;
}

.marker {
	stroke: rgb(var(--v-theme-surface));
	stroke-width: 0.6;
}

.marker-pulse {
	opacity: 0.25;
	animation: world-map-pulse 2s ease-out infinite;
	transform-origin: center;
	transform-box: fill-box;
}

@keyframes world-map-pulse {
	0% {
		opacity: 0.35;
		transform: scale(0.5);
	}
	100% {
		opacity: 0;
		transform: scale(1.3);
	}
}

@media (prefers-reduced-motion: reduce) {
	.marker-pulse {
		animation: none;
		opacity: 0.2;
	}
}
</style>
