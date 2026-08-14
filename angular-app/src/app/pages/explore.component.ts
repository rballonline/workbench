import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-explore',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<div><h2 class="text-3xl font-bold">Explore</h2><p>Search and discover new cities</p></div>`
})
export class ExploreComponent {}
