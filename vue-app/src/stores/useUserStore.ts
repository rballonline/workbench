import { defineStore } from 'pinia'

/**
 * Identity is client-side only — `AddDestinationRequest.addedBy` is a free-text field
 * with no auth behind it, so this is just a remembered display name.
 */
export const useUserStore = defineStore('userStore', {
  state: () => ({
    displayName: '' as string,
    locale: 'en' as string,
    theme: 'light' as 'light' | 'dark',
  }),

  getters: {
    /** What to send as `addedBy`; the backend caps it at 100 chars. */
    attribution: (state): string | undefined =>
      state.displayName.trim() ? state.displayName.trim().slice(0, 100) : undefined,

    hasName: state => state.displayName.trim().length > 0,
  },

  actions: {
    setDisplayName (name: string) {
      this.displayName = name
    },
    setLocale (locale: string) {
      this.locale = locale
    },
    toggleTheme () {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
    },
  },

  persist: {
    pick: ['displayName', 'locale', 'theme'],
  },
})
