import {Component, inject, OnInit} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {AuthService} from '../../../services/auth/auth.service';
import {User} from '../../../models/user';
import {LogoComponent} from '../logo/logo.component';

@Component({
  selector: 'app-header',
  imports: [
    RouterLink,
    RouterLinkActive,
    LogoComponent
  ],
  templateUrl: './header.component.html',
  standalone: true,
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit {

  authService: AuthService = inject(AuthService);
  user:User|null = null;
  authInitialized: boolean = false;

  links: any[] = [
    { name: 'Statistiques', path: '/tableau-de-bord' },
    { name: 'Trouver une borne', path: '/tableau-de-bord/trouver-bornes' },
    { name: 'Mes réservations', path: '/tableau-de-bord/mes-reservations' },
    { name: 'Demandes de réservations', path: '/tableau-de-bord/mes-demandes-de-reservation' },
    { name: 'Lieux de recharge', path: '/tableau-de-bord/lieux-de-recharge' },
    { name: 'Mes voitures', path: '/tableau-de-bord/mes-voitures' }
  ]

  ngOnInit(): void {
   this.authService.initialized$.subscribe(initialized => {
      this.authInitialized = initialized;
    });

    this.authService.user$.subscribe(user => {
      this.user = user; // Always update, even if null
      console.log('User loaded:', this.user);
      this.authInitialized = true;
    });
  }


  logout() {
    this.authService.logout();

  }
}
