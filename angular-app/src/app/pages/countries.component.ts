import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-countries',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<div><h2 class="text-3xl font-bold">Countries</h2><p>Browse countries</p></div>`
})
export class CountriesComponent {}
