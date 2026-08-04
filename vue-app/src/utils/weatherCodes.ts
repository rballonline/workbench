/**
 * WMO 4677 weather codes, as returned by Open-Meteo and passed through by
 * `WeatherResponse.weatherCode`. Grouped rather than enumerated one-per-code so the
 * locale files stay a manageable size.
 */

export interface WeatherDescriptor {
  /** Key under `weather.codes.*` in the locale files. */
  key: string
  icon: string
}

const GROUPS: Array<{ codes: number[], descriptor: WeatherDescriptor }> = [
  { codes: [0], descriptor: { key: 'clear', icon: 'mdi-weather-sunny' } },
  { codes: [1], descriptor: { key: 'mainlyClear', icon: 'mdi-weather-partly-cloudy' } },
  { codes: [2], descriptor: { key: 'partlyCloudy', icon: 'mdi-weather-partly-cloudy' } },
  { codes: [3], descriptor: { key: 'overcast', icon: 'mdi-weather-cloudy' } },
  { codes: [45, 48], descriptor: { key: 'fog', icon: 'mdi-weather-fog' } },
  { codes: [51, 53, 55, 56, 57], descriptor: { key: 'drizzle', icon: 'mdi-weather-rainy' } },
  { codes: [61, 63, 65, 66, 67], descriptor: { key: 'rain', icon: 'mdi-weather-pouring' } },
  { codes: [71, 73, 75, 77], descriptor: { key: 'snow', icon: 'mdi-weather-snowy' } },
  { codes: [80, 81, 82], descriptor: { key: 'showers', icon: 'mdi-weather-rainy' } },
  { codes: [85, 86], descriptor: { key: 'snowShowers', icon: 'mdi-weather-snowy-heavy' } },
  { codes: [95, 96, 99], descriptor: { key: 'thunderstorm', icon: 'mdi-weather-lightning' } },
]

const UNKNOWN: WeatherDescriptor = { key: 'unknown', icon: 'mdi-weather-cloudy-alert' }

export function describeWeatherCode (code: number | null | undefined): WeatherDescriptor {
  if (code == null) {
    return UNKNOWN
  }
  return GROUPS.find(group => group.codes.includes(code))?.descriptor ?? UNKNOWN
}

/** Vuetify colour used to tint the temperature readout. */
export function temperatureColor (celsius: number | null | undefined): string {
  if (celsius == null) {
    return 'grey'
  }
  if (celsius <= 0) {
    return 'light-blue'
  }
  if (celsius <= 12) {
    return 'cyan'
  }
  if (celsius <= 24) {
    return 'teal'
  }
  if (celsius <= 32) {
    return 'orange'
  }
  return 'deep-orange'
}

export function toFahrenheit (celsius: number): number {
  return celsius * 9 / 5 + 32
}
