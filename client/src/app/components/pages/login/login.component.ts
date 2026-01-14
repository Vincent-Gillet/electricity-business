import {Component, inject} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AuthService} from '../../../services/auth/auth.service';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ErrorFromComponent} from '../../parts/error-from/error-from.component';
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
export class LoginComponent {
  private authService: AuthService = inject(AuthService);
  private route: ActivatedRoute = inject(ActivatedRoute);
  private router: Router = inject(Router);

  loginForm: FormGroup;
  isSubmitted = false;
  isLoading = false;
  errorMessage: string = '';

  constructor(private fb: FormBuilder) {
    this.loginForm = this.fb.group({
      emailUser: ['', [Validators.required, Validators.email]],
      passwordUser: ['', [Validators.required, Validators.minLength(8), Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$')]], // contrainte MINLENGTH 8 et pattern (au moins une majuscule, une minuscule, un chiffre et un caractère spécial)
    });
  }

  onSubmit(): void {
    this.isSubmitted = true;

    if (this.loginForm.valid) {
      this.isLoading = true;
      const { emailUser, passwordUser } = this.loginForm.value;

      this.authService.authenticate(emailUser, passwordUser).subscribe({
        next: (response) => {
          localStorage.setItem("access_token", response.accessToken);

          // Attend 50ms pour s'assurer que le token est bien stocké
          setTimeout(() => {
            const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/tableau-de-bord/mes-informations';
            this.router.navigateByUrl(returnUrl).then((success) => {
              if (!success) {
                console.error('Échec de la redirection vers:', returnUrl);
                this.router.navigateByUrl('/tableau-de-bord/mes-informations');
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
}
