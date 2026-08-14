import { Component, ChangeDetectionStrategy } from '@angular/core';
import { AppShellComponent } from './shell/app-shell.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AppShellComponent],
  template: '<app-shell></app-shell>',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {}
