import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {LogoComponent} from '../logo/logo.component';

@Component({
  selector: 'app-footer',
  imports: [
    RouterLink,
    LogoComponent
  ],
  templateUrl: './footer.component.html',
  standalone: true,
  styleUrl: './footer.component.scss'
})
export class FooterComponent {
}
