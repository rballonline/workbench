import { createRouter, createWebHistory } from 'vue-router'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/components/pages/HomePage.vue'),
    },
    {
      path: '/wishlist',
      name: 'wishlist',
      component: () => import('@/components/pages/WishlistPage.vue'),
    },
    {
      path: '/explore',
      name: 'explore',
      component: () => import('@/components/pages/ExplorePage.vue'),
    },
    {
      path: '/weather',
      name: 'weather',
      component: () => import('@/components/pages/WeatherPage.vue'),
    },
    {
      path: '/countries',
      name: 'countries',
      component: () => import('@/components/pages/CountriesPage.vue'),
    },
    {
      path: '/iss',
      name: 'iss',
      component: () => import('@/components/pages/IssTrackerPage.vue'),
    },
    {
      path: '/live',
      name: 'live',
      component: () => import('@/components/pages/LiveFeedPage.vue'),
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/components/pages/SettingsPage.vue'),
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

// `?api=<origin>` points the whole app at another backend (see useAppContext) and
// must survive in-app navigation, or a shared/bookmarked link to a non-default
// backend loses that override the moment someone clicks a nav item.
router.beforeEach((to, from) => {
  if (!to.query.api && from.query.api) {
    return { ...to, query: { ...to.query, api: from.query.api } }
  }
})
