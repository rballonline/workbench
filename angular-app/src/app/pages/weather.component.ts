import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-weather',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<div><h2 class="text-3xl font-bold">Weather</h2><p>Weather information</p></div>`
})
export class WeatherComponent {}
