import { onUnmounted, ref } from 'vue'

export const DEFAULT_PAGE = 'welcome'

/** Query params owned by a page, cleared whenever navigation leaves that page. */
const PAGE_PARAMS = ['city', 'country'] as const

/**
 * No Vue Router — the page lives in `?page=` and is swapped with `history.pushState`,
 * which preserves any other query params (notably `?api=`, read by `useAppContext`).
 */
export function useAppNav () {
  const currentPage = ref(new URLSearchParams(window.location.search).get('page') ?? DEFAULT_PAGE)

  /**
   * `extra` seeds page-scoped params — e.g. `navigate('weather', { city: 'Tokyo' })`
   * hands the destination straight to the weather page.
   */
  function navigate (page: string, extra?: Record<string, string>) {
    const params = new URLSearchParams(window.location.search)
    params.set('page', page)
    for (const key of PAGE_PARAMS) {
      params.delete(key)
    }
    for (const [key, value] of Object.entries(extra ?? {})) {
      params.set(key, value)
    }

    history.pushState({}, '', `${window.location.pathname}?${params}`)
    currentPage.value = page
  }

  function onPopState () {
    currentPage.value = new URLSearchParams(window.location.search).get('page') ?? DEFAULT_PAGE
  }

  window.addEventListener('popstate', onPopState)
  onUnmounted(() => window.removeEventListener('popstate', onPopState))

  return { currentPage, navigate }
}

/** Read a page-scoped param, for pages that can be deep-linked into. */
export function readPageParam (key: (typeof PAGE_PARAMS)[number]): string | null {
  return new URLSearchParams(window.location.search).get(key)
}
