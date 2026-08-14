import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  {
    path: 'home',
    loadComponent: () => import('./pages/home.component').then(m => m.HomeComponent),
    data: { title: 'Home' }
  },
  {
    path: 'wishlist',
    loadComponent: () => import('./pages/wishlist.component').then(m => m.WishlistComponent),
    data: { title: 'Wishlist' }
  },
  {
    path: 'explore',
    loadComponent: () => import('./pages/explore.component').then(m => m.ExploreComponent),
    data: { title: 'Explore' }
  },
  {
    path: 'weather',
    loadComponent: () => import('./pages/weather.component').then(m => m.WeatherComponent),
    data: { title: 'Weather' }
  },
  {
    path: 'countries',
    loadComponent: () => import('./pages/countries.component').then(m => m.CountriesComponent),
    data: { title: 'Countries' }
  },
  {
    path: 'iss',
    loadComponent: () => import('./pages/iss-tracker.component').then(m => m.IssTrackerComponent),
    data: { title: 'ISS Tracker' }
  },
  {
    path: 'live',
    loadComponent: () => import('./pages/live-feed.component').then(m => m.LiveFeedComponent),
    data: { title: 'Live Feed' }
  },
  {
    path: 'settings',
    loadComponent: () => import('./pages/settings.component').then(m => m.SettingsComponent),
    data: { title: 'Settings' }
  },
  { path: '**', redirectTo: 'home' }
];
