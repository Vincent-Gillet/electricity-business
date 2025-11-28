import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AuthService} from '../../../services/auth/auth.service';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ErrorFromComponent} from '../../parts/error-from/error-from.component';
import {empty} from 'rxjs';

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

  ngOnInit() {
    this.authService.user$.subscribe(user => {
      if (user) {
        this.handleRedirect();
      }
    });
  }

  onSubmit():void {
    this.isSubmitted = true;

    if (this.loginForm.valid) {
      this.isLoading = true;
      const loginData = this.loginForm.value;

      this.authService.authenticate(loginData).subscribe(
        {
          next: (response) => {
            sessionStorage.setItem("tokenStorage", JSON.stringify(response));
            const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/tableau-de-bord/mes-informations';
            this.handleRedirect();
            this.authService.verifyAuth(returnUrl);
          },
          error: (error) => {
            this.errorMessage = 'Identifiants invalides. Veuillez réessayer.';
            console.error('Error login user:', error);
          }
        }
      );
    }
  }

  private handleRedirect(): void {
    const returnUrl = this.route.snapshot.queryParams['returnUrl'];
    if (returnUrl) {
      const decodedReturnUrl = decodeURIComponent(returnUrl);
      const finalUrl = decodedReturnUrl.startsWith('/') ? decodedReturnUrl : `/${decodedReturnUrl}`;
      this.router.navigateByUrl(finalUrl);
    } else {
      this.router.navigateByUrl('/tableau-de-bord/mes-informations');
    }
  }

  protected readonly empty = empty;
}
