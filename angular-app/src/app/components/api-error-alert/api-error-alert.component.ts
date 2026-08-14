import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import type { ApiFailure } from '../../core/services/api.service';

@Component({
  selector: 'app-api-error-alert',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="p-4 rounded bg-red-50 border border-red-200">
      <h3 class="font-semibold text-red-800 mb-2">
        {{ getErrorTitle(error().kind) }}
      </h3>
      <p class="text-sm text-red-700">{{ error().detail }}</p>
    </div>
  `,
  styles: []
})
export class ApiErrorAlertComponent {
  readonly error = input.required<ApiFailure>();

  getErrorTitle(kind: string): string {
    const titles: Record<string, string> = {
      unreachable: 'Backend Unreachable',
      notFound: 'Not Found',
      validation: 'Validation Error',
      other: 'Error'
    };
    return titles[kind] || 'Error';
  }
}
