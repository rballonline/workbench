import { Component, OnInit, OnDestroy, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import type { LiveMessage } from '@shared/types';
import { isDestinationEvent } from '@shared/types';
import { LiveUpdatesService } from '../core/services/live-updates.service';
import { DestinationsStore } from '../store/destinations.store';
import { IssStore } from '../store/iss.store';
import { AppSidebarComponent } from './app-sidebar.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterModule, AppSidebarComponent],
  template: `
    <div class="flex h-screen bg-gray-100">
      <!-- Sidebar -->
      <app-sidebar class="w-64 border-r border-gray-200 bg-white"></app-sidebar>
      
      <!-- Main content -->
      <div class="flex-1 flex flex-col">
        <!-- Header -->
        <header class="bg-white border-b border-gray-200 shadow-sm px-6 py-4">
          <h1 class="text-2xl font-bold text-gray-900">Places I'd Like to Visit</h1>
        </header>
        
        <!-- Content area -->
        <main class="flex-1 overflow-auto p-6">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppShellComponent implements OnInit, OnDestroy {
  private readonly liveUpdates = inject(LiveUpdatesService);
  private readonly destinationsStore = inject(DestinationsStore);
  private readonly issStore = inject(IssStore);

  private unsubscribe: (() => void) | null = null;

  ngOnInit(): void {
    // Connect WebSocket and subscribe to messages
    this.liveUpdates.connect();
    this.unsubscribe = this.liveUpdates.onMessage((msg: LiveMessage) => {
      if (isDestinationEvent(msg)) {
        this.destinationsStore.applyEvent(msg);
      } else {
        // IssPosition
        this.issStore.recordPosition(msg);
      }
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe?.();
    this.liveUpdates.disconnect();
  }
}
