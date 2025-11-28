import {Component, inject, OnInit} from '@angular/core';
import {NavigationStart, Router, RouterOutlet} from '@angular/router';
import {HeaderComponent} from './components/parts/header/header.component';
import {FooterComponent} from './components/parts/footer/footer.component';
import {GlobalErrorService} from './services/global-error/global-error.service';
import {GlobalErrorComponent} from './components/parts/global-error/global-error/global-error.component';
import {AsyncPipe} from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    HeaderComponent,
    RouterOutlet,
    FooterComponent,
    GlobalErrorComponent,
    AsyncPipe
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'client';

  private globalError: GlobalErrorService = inject(GlobalErrorService);
  private router: Router = inject(Router);

  currentError$ = this.globalError.error$;

  ngOnInit() {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.globalError.clearError();
      }
    });
  }

  onRetry() {
    this.globalError.clearError();
    window.location.reload();
  }

}
