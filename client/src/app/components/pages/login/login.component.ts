import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AuthService} from '../../../services/auth/auth.service';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ErrorFromComponent} from '../../parts/error-from/error-from.component';
import {empty} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    ErrorFromComponent,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  authService: AuthService = inject(AuthService);
  private route: ActivatedRoute = inject(ActivatedRoute);
  loginForm: FormGroup;
  isSubmitted = false;
  isLoading = false;
  errorMessage: string = '';

  constructor(private fb: FormBuilder, private router: Router) {
    this.loginForm = this.fb.group({
      emailUser: ['', [Validators.required, Validators.email]],
      passwordUser: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

/*  ngOnInit() {
    this.authService.user$.subscribe(user => {
      if (user) {
        this.handleRedirect();
      }
    });
  }*/

  ngOnInit() {
/*    const token = localStorage.getItem("access_token");
    if (token) {
      this.authService.verifyAuth().subscribe({
        next: (user) => {
          if (user) {
            const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || undefined;
            this.handleRedirect(returnUrl);
          }
        },
        error: (err) => {
          console.error('Token invalide:', err);
          localStorage.removeItem("access_token");
        }
      });
    }*/
  }

/*  onSubmit():void {
    this.isSubmitted = true;

    if (this.loginForm.valid) {
      this.isLoading = true;
      const loginData = this.loginForm.value;

      this.authService.authenticate(loginData.emailUser, loginData.passwordUser).subscribe(
        {
          next: (response) => {
            localStorage.setItem("access_token", response.accessToken);
            const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/tableau-de-bord/mes-informations';
            this.handleRedirect(returnUrl);
/!*
            this.authService.verifyAuth(returnUrl);
*!/
          },
          error: (error) => {
            this.errorMessage = 'Identifiants invalides. Veuillez réessayer.';
            console.error('Error login user:', error);
          }
        }
      );
    }
  }*/

  onSubmit(): void {
    this.isSubmitted = true;

    if (this.loginForm.valid) {
      this.isLoading = true;
      const { emailUser, passwordUser } = this.loginForm.value;

      this.authService.authenticate(emailUser, passwordUser).subscribe({
        next: (response) => {
          localStorage.setItem("access_token", response.accessToken);

          // ⏳ Attend 50ms pour s'assurer que le token est bien stocké
          setTimeout(() => {
            const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/tableau-de-bord/mes-informations';
            this.router.navigateByUrl(returnUrl).then((success) => {
              if (!success) {
                console.error('Échec de la redirection vers:', returnUrl);
                this.router.navigateByUrl('/tableau-de-bord/mes-informations'); // Fallback
              }
            });
          }, 50);
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = err.status === 401
            ? 'Email ou mot de passe incorrect.'
            : 'Une erreur est survenue. Veuillez réessayer.';
          this.isLoading = false;
          console.error('Erreur de connexion:', err);
        }
      });
    }
  }



/*
  private handleRedirect(): void {
    const returnUrl = this.route.snapshot.queryParams['returnUrl'];
    if (returnUrl) {
      const decodedReturnUrl = decodeURIComponent(returnUrl);
      const finalUrl = decodedReturnUrl.startsWith('/') ? decodedReturnUrl : `/${decodedReturnUrl}`;
      this.router.navigateByUrl(finalUrl);
    } else {
      this.router.navigateByUrl('/tableau-de-bord/mes-informations');
    }
  }*/
  private handleRedirect(returnUrl?: string): void {
      const url = returnUrl ||
        this.route.snapshot.queryParamMap.get('returnUrl') ||
        '/tableau-de-bord/mes-informations';

      console.log('URL avant décodage:', url);
      const decodedUrl = decodeURIComponent(url);
      console.log('URL après décodage:', decodedUrl);

      const finalUrl = decodedUrl.startsWith('/') ? decodedUrl : `/${decodedUrl}`;

      this.router.navigateByUrl(finalUrl).then(success => {
        if (!success) {
          console.error('Échec de la redirection vers:', finalUrl);
          this.router.navigateByUrl('/tableau-de-bord/mes-informations'); // Fallback
        } else {
          console.log('Redirection réussie vers:', finalUrl);
        }
      });
    }

  protected readonly empty = empty;
}
