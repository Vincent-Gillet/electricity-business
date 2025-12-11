import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {AuthService} from '../../../services/auth/auth.service';
import {User} from '../../../models/user';
import {LogoComponent} from '../logo/logo.component';
import {combineLatest, Subject, takeUntil} from 'rxjs';

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
export class HeaderComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  authService: AuthService = inject(AuthService);
  user:User|null = null;
  authInitialized: boolean = false;

  links: any[] = [
    { name: 'Trouver une borne', path: '/tableau-de-bord/trouver-bornes', ariaLabel: 'Trouver une borne' },
    { name: 'Mes réservations', path: '/tableau-de-bord/mes-reservations', ariaLabel: 'Voir mes réservations' },
    { name: 'Demandes de réservations', path: '/tableau-de-bord/mes-demandes-de-reservation', ariaLabel: 'Voir mes demandes de réservations' },
    { name: 'Lieux de recharge', path: '/tableau-de-bord/lieux-de-recharge', ariaLabel: 'Voir mes lieux de recharge' },
    { name: 'Mes voitures', path: '/tableau-de-bord/mes-voitures', ariaLabel: 'Voir mes voitures' },
    { name: 'Mes options', path: '/tableau-de-bord/mes-options', ariaLabel: 'Voir mes options' },
  ]

/*  ngOnInit(): void {
   this.authService.initialized$.subscribe(initialized => {
      this.authInitialized = initialized;
    });

    this.authService.user$.subscribe(user => {
      this.user = user;
      console.log('User loaded:', this.user);
      this.authInitialized = true;
    });
  }*/

  ngOnInit(): void {
    combineLatest([
      this.authService.initialized$,
      this.authService.user$
    ]).pipe(
      takeUntil(this.destroy$)
    ).subscribe(([initialized, user]) => {
      this.authInitialized = initialized;
      this.user = user;
      console.log('Header updated - Initialized:', initialized, 'User:', user);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get isLoggedIn(): boolean {
    return !!this.user;
  }


  logout() {
    this.authService.logout();

  }
}
