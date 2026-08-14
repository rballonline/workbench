import { Injectable, inject, signal, computed } from '@angular/core';
import type { Destination, DestinationEvent, AddDestinationRequest } from '@shared/types';
import { ApiService, type ApiFailure } from '../core/services/api.service';

@Injectable({ providedIn: 'root' })
export class DestinationsStore {
  private readonly apiService = inject(ApiService);

  // Private state signals
  private readonly _items = signal<Destination[]>([]);
  private readonly _loading = signal(false);
  private readonly _saving = signal(false);
  private readonly _error = signal<ApiFailure | null>(null);
  private readonly _remoteIds = signal<number[]>([]);

  // Public read-only signals
  readonly items = this._items.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly saving = this._saving.asReadonly();
  readonly error = this._error.asReadonly();
  readonly remoteIds = this._remoteIds.asReadonly();

  // Computed signals (derived state)
  readonly count = computed(() => this._items().length);

  readonly sorted = computed(() =>
    this._items().toSorted((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
  );

  readonly byRegion = computed(() => {
    const groups: Record<string, Destination[]> = {};
    for (const d of this._items()) {
      const region = d.country?.region ?? 'Unknown';
      (groups[region] ??= []).push(d);
    }
    return groups;
  });

  readonly contributors = computed(() => {
    const names = new Set<string>();
    for (const d of this._items()) {
      if (d.addedBy) names.add(d.addedBy);
    }
    return [...names].toSorted();
  });

  // Actions
  async load(): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    try {
      const destinations = await this.apiService.listDestinations();
      this._items.set(destinations);
    } catch (error) {
      this._error.set(this.classifyError(error));
    } finally {
      this._loading.set(false);
    }
  }

  async add(request: AddDestinationRequest): Promise<Destination | null> {
    this._saving.set(true);
    this._error.set(null);
    try {
      const created = await this.apiService.addDestination(request);
      this.upsert(created);
      return created;
    } catch (error) {
      this._error.set(this.classifyError(error));
      return null;
    } finally {
      this._saving.set(false);
    }
  }

  async remove(id: number): Promise<void> {
    this._error.set(null);
    try {
      await this.apiService.deleteDestination(id);
      this.dropById(id);
    } catch (error) {
      this._error.set(this.classifyError(error));
    }
  }

  async applyEvent(event: DestinationEvent): Promise<void> {
    const { id } = event.destination;

    if (event.action === 'DELETED') {
      this.dropById(id);
      return;
    }

    // Skip if we already have the enriched response from our own POST
    if (event.action === 'CREATED' && this._items().some(d => d.id === id)) {
      return;
    }

    try {
      const destination = await this.apiService.getDestination(id);
      this.upsert(destination);
      if (!this._remoteIds().includes(id)) {
        this._remoteIds.update(ids => [...ids, id]);
      }
    } catch {
      // Refetch failure only means this client stays slightly stale;
      // the next full load reconciles. Not worth surfacing as a page-level error.
    }
  }

  upsert(destination: Destination): void {
    this._items.update(items => {
      const index = items.findIndex(d => d.id === destination.id);
      if (index === -1) {
        return [...items, destination];
      } else {
        return items.map((d, i) => i === index ? destination : d);
      }
    });
  }

  dropById(id: number): void {
    this._items.update(items => items.filter(d => d.id !== id));
    this._remoteIds.update(ids => ids.filter(remoteId => remoteId !== id));
  }

  clearError(): void {
    this._error.set(null);
  }

  private classifyError(error: unknown): ApiFailure {
    return this.apiService.classifyError(error);
  }
}
