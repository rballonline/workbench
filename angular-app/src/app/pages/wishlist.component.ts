import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import type { AddDestinationRequest } from '@shared/types';
import { DestinationsStore } from '../store/destinations.store';
import { DestinationCardComponent } from '../components/destination-card/destination-card.component';
import { ApiErrorAlertComponent } from '../components/api-error-alert/api-error-alert.component';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, DestinationCardComponent, ApiErrorAlertComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div>
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-3xl font-bold">My Wishlist</h2>
        <button 
          (click)="openAddDialog()" 
          class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          + Add Destination
        </button>
      </div>

      @if (store.loading()) {
        <p class="text-gray-600">Loading destinations...</p>
      } @else if (store.error()) {
        <app-api-error-alert [error]="store.error()!"></app-api-error-alert>
      } @else {
        <p class="text-gray-600 mb-4">Total: {{ store.count() }}</p>
        @if (store.count() === 0) {
          <p class="text-gray-500 italic">No destinations yet. Add one to get started!</p>
        } @else {
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            @for (dest of store.sorted(); track dest.id) {
              <app-destination-card 
                [destination]="dest"
                (removeClicked)="onRemoveClicked($event)"
              ></app-destination-card>
            }
          </div>
        }
      }
    </div>
  `
})
export class WishlistComponent {
  protected readonly store = inject(DestinationsStore);

  constructor() {
    this.store.load();
  }

  openAddDialog(): void {
    // TODO: Implement dialog for adding destination
    alert('Add destination dialog - to be implemented with CDK overlay');
  }

  onRemoveClicked(id: number): void {
    if (confirm('Remove this destination?')) {
      this.store.remove(id);
    }
  }
}
