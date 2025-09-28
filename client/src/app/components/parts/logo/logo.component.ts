import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-logo',
  standalone: true,
  imports: [
    RouterLink
  ],
  templateUrl: './logo.component.html',
  styleUrl: './logo.component.scss'
})
export class LogoComponent {
  imgLogo = 'assets/images/logo_Electricity_Business.svg';
}
