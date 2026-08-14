import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DestinationsStore } from '../store/destinations.store';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div>
      <h2 class="text-3xl font-bold mb-4">Home</h2>
      @if ((store.byRegion() | keyvalue).length > 0) {
        @for (region of store.byRegion() | keyvalue; track region.key) {
          <div class="mb-6">
            <h3 class="text-xl font-semibold mb-2">{{ region.key }}</h3>
            <p class="text-gray-600">{{ region.value.length }} destinations</p>
          </div>
        }
      } @else {
        <p class="text-gray-500 italic">No destinations yet</p>
      }
    </div>
  `
})
export class HomeComponent {
  protected readonly store = inject(DestinationsStore);

  constructor() {
    this.store.load();
  }
}
