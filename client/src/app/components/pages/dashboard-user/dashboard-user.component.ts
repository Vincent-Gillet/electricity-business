import { Component } from '@angular/core';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-dashboard-user',
  standalone: true,
  imports: [
    RouterOutlet
  ],
  templateUrl: './dashboard-user.component.html',
  styleUrl: './dashboard-user.component.scss'
})
export class DashboardUserComponent {

}
