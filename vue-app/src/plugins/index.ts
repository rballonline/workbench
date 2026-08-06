/**
 * plugins/index.ts
 *
 * Automatically included in `./src/main.ts`
 */

import type { App } from 'vue'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { router } from '@/router'
import pinia from '../stores'
import i18n from './i18n'
// Plugins
import vuetify from './vuetify'

pinia.use(piniaPluginPersistedstate)

export function registerPlugins (app: App) {
  app
    .use(vuetify)
    .use(i18n)
    .use(pinia)
    .use(router)
}
