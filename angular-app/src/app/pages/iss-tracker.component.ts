import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IssStore } from '../store/iss.store';

@Component({
  selector: 'app-iss-tracker',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div>
      <h2 class="text-3xl font-bold mb-4">ISS Tracker</h2>
      @if (store.position()) {
        <p>Latitude: {{ store.position()!.latitude | number }}</p>
        <p>Longitude: {{ store.position()!.longitude | number }}</p>
      }
    </div>
  `
})
export class IssTrackerComponent {
  protected readonly store = inject(IssStore);

  constructor() {
    this.store.fetchOnce();
  }
}
