import { Injectable, signal, computed, inject } from '@angular/core';
import type { IssPosition } from '@shared/types';
import { ApiService, type ApiFailure } from '../core/services/api.service';

export interface TrailSegment {
  from: IssPosition;
  to: IssPosition;
  crossesAntimeridian: boolean;
}

@Injectable({ providedIn: 'root' })
export class IssStore {
  private readonly apiService = inject(ApiService);

  private readonly _position = signal<IssPosition | null>(null);
  private readonly _trail = signal<IssPosition[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<ApiFailure | null>(null);

  readonly position = this._position.asReadonly();
  readonly trail = this._trail.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  // Compute trail segments for map rendering (antimeridian-aware)
  readonly trailSegments = computed(() => {
    const points = this._trail();
    const segments: TrailSegment[] = [];

    for (let i = 1; i < points.length; i++) {
      const from = points[i - 1];
      const to = points[i];
      const crossesAntimeridian = Math.abs(to.longitude - from.longitude) > 180;

      segments.push({ from, to, crossesAntimeridian });
    }

    return segments;
  });

  async fetchOnce(): Promise<void> {
    if (this._position()) return; // Already loaded

    this._loading.set(true);
    this._error.set(null);
    try {
      const pos = await this.apiService.getIssPosition();
      this._position.set(pos);
      this._trail.set([pos]);
    } catch (error) {
      this._error.set(this.classifyError(error));
    } finally {
      this._loading.set(false);
    }
  }

  recordPosition(position: IssPosition): void {
    this._position.set(position);
    this._trail.update(trail => {
      const newTrail = [position, ...trail];
      // Keep last 50 points for map rendering
      return newTrail.slice(0, 50);
    });
  }

  clearTrail(): void {
    this._trail.set([]);
  }

  clearError(): void {
    this._error.set(null);
  }

  private classifyError(error: unknown): ApiFailure {
    return this.apiService.classifyError(error);
  }
}
