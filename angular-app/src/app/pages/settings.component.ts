import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserStore } from '../store/user.store';
import { AppContextService } from '../core/services/app-context.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div>
      <h2 class="text-3xl font-bold mb-4">Settings</h2>
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium">Theme</label>
          <p class="text-gray-600">{{ userStore.theme() }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium">API Override</label>
          <p class="text-gray-600">{{ appContext.overridden ? 'Custom' : 'Default' }}</p>
        </div>
      </div>
    </div>
  `
})
export class SettingsComponent {
  protected readonly userStore = inject(UserStore);
  protected readonly appContext = inject(AppContextService);
}
