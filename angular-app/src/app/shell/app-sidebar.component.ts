import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DestinationsStore } from '../store/destinations.store';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="flex flex-col h-full">
      <!-- Nav items -->
      <nav class="flex-1 px-4 py-6 space-y-2">
        <a 
          routerLink="/home" 
          routerLinkActive="bg-blue-100 text-blue-700"
          [routerLinkActiveOptions]="{ exact: true }"
          class="block px-4 py-2 rounded hover:bg-gray-100 transition"
        >
          🏠 Home
        </a>
        <a 
          routerLink="/wishlist" 
          routerLinkActive="bg-blue-100 text-blue-700"
          class="block px-4 py-2 rounded hover:bg-gray-100 transition relative"
        >
          ♥️ Wishlist
          @if (destinationsStore.count() > 0) {
            <span class="absolute right-4 top-2 bg-red-500 text-white text-xs rounded-full px-2 py-1">
              {{ destinationsStore.count() }}
            </span>
          }
        </a>
        <a 
          routerLink="/explore" 
          routerLinkActive="bg-blue-100 text-blue-700"
          class="block px-4 py-2 rounded hover:bg-gray-100 transition"
        >
          🔍 Explore
        </a>
        <a 
          routerLink="/weather" 
          routerLinkActive="bg-blue-100 text-blue-700"
          class="block px-4 py-2 rounded hover:bg-gray-100 transition"
        >
          🌤️ Weather
        </a>
        <a 
          routerLink="/countries" 
          routerLinkActive="bg-blue-100 text-blue-700"
          class="block px-4 py-2 rounded hover:bg-gray-100 transition"
        >
          🌍 Countries
        </a>
        <a 
          routerLink="/iss" 
          routerLinkActive="bg-blue-100 text-blue-700"
          class="block px-4 py-2 rounded hover:bg-gray-100 transition"
        >
          🛰️ ISS Tracker
        </a>
        <a 
          routerLink="/live" 
          routerLinkActive="bg-blue-100 text-blue-700"
          class="block px-4 py-2 rounded hover:bg-gray-100 transition"
        >
          📡 Live Feed
        </a>
        <a 
          routerLink="/settings" 
          routerLinkActive="bg-blue-100 text-blue-700"
          class="block px-4 py-2 rounded hover:bg-gray-100 transition"
        >
          ⚙️ Settings
        </a>
      </nav>
    </aside>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppSidebarComponent {
  protected readonly destinationsStore = inject(DestinationsStore);
}
