import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LiveUpdatesService } from '../core/services/live-updates.service';

@Component({
  selector: 'app-live-feed',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div>
      <h2 class="text-3xl font-bold mb-4">Live Feed</h2>
      <p class="text-gray-600 mb-2">Status: {{ liveUpdates.status() }}</p>
      @if (liveUpdates.log().length > 0) {
        <div class="space-y-2">
          @for (entry of liveUpdates.log(); track entry.id) {
            <div class="bg-white p-2 rounded border border-gray-200 text-sm">
              {{ entry.message | json }}
            </div>
          }
        </div>
      }
    </div>
  `
})
export class LiveFeedComponent {
  protected readonly liveUpdates = inject(LiveUpdatesService);

  constructor() {
    this.liveUpdates.connect();
  }
}
