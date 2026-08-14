import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import type { Destination } from '@shared/types';

@Component({
  selector: 'app-destination-card',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="border rounded-lg p-4 bg-white shadow hover:shadow-lg transition">
      <h3 class="text-lg font-bold text-gray-900">{{ destination().cityName }}</h3>
      <p class="text-sm text-gray-600">{{ destination().country.name }}</p>
      <p class="text-xs text-gray-500 mt-2">
        📍 {{ destination().latitude | number: '1.2-2' }}, 
        {{ destination().longitude | number: '1.2-2' }}
      </p>
      <p class="text-xs text-gray-400 mt-2">
        Added by: {{ destination().addedBy ?? 'Anonymous' }}
      </p>
      <p class="text-xs text-gray-400">
        {{ destination().createdAt | date: 'short' }}
      </p>
      <button 
        (click)="onRemove()"
        class="mt-4 w-full px-3 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition text-sm"
      >
        Remove
      </button>
    </div>
  `,
  styles: []
})
export class DestinationCardComponent {
  readonly destination = input.required<Destination>();
  readonly removeClicked = output<number>();

  onRemove(): void {
    this.removeClicked.emit(this.destination().id);
  }
}
