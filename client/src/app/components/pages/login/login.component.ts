import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../../../services/auth/auth.service';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ErrorFromComponent} from '../../parts/error-from/error-from.component';

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

  authService: AuthService = inject(AuthService);

  // Propriété représentant le formulaire
  loginForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;


  constructor(private fb: FormBuilder, private router: Router) {
    //Création du form
    this.loginForm = this.fb.group({

      emailUser: ['', [Validators.required, Validators.email]],  // Champ nommé email, requis, contrainte EMAIL
      passwordUser: ['', [Validators.required, Validators.minLength(4)]] // Champ nommé email, requis, contrainte longueur
    });
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.loginForm.value);

    this.isSubmitted = true;

     console.log("MON FORM EST SOUMIS");
     console.log("loginForm.valid ",this.loginForm.valid);
     console.log("Toutes les valeurs des control du groupe -> loginForm.value ",this.loginForm.value);
     console.log("Recuperer un seul control avec loginForm.get('email')",this.loginForm.get("passwordUser"));
     console.log("Recuperer la validité d'un control avec loginForm.get('email').valid",this.loginForm.get("emailUser")?.valid);
     console.log("Recuperer les erreurs d'un control avec loginForm.get('motDePasse').errors",this.loginForm.get("passwordUser")?.errors);
     console.log("Recuperer un seul control avec loginForm.get('motDePasse')",this.loginForm.get("passwordUser"));


    if (this.loginForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const loginData = this.loginForm.value;

      console.log('Données de connexion:', loginData);

      // 5. Simuler un appel API avec setTimeout
      // (Dans un vrai projet, ça serait un appel HTTP)
      setTimeout(() => {

        this.authService.authenticate(loginData).subscribe(
          {
            next: (response) => {
              console.log('User login successfully:', response);
              localStorage.setItem("tokenStorage", JSON.stringify(response));

              this.authService.verifyAuth("/profil");
            },
            error: (error) => {
              console.error('Error login user:', error);
            }
          }
        );

      }, 2000);

    }

  }

}
