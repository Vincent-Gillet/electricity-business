import {Component, inject, OnInit} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {UserService} from '../../../services/user/user.service';
import {ErrorFromComponent} from '../../parts/error-from/error-from.component';

import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';

@Component({
  selector: 'app-subscription',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    ErrorFromComponent
  ],
  templateUrl: './subscription.component.html',
  styleUrl: './subscription.component.scss'
})
export class SubscriptionComponent {

  userService: UserService = inject(UserService);

  // Propriété représentant le formulaire
  subscirbeForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;


  constructor(private fb: FormBuilder, private router: Router) {
    //Création du form
    this.subscirbeForm = this.fb.group(
      {
        firstName: ['', [Validators.required]],
        surnameUser: ['', [Validators.required]],
        username: ['', [Validators.required]],
        dateOfBirth: ['', [Validators.required]],
        phone: ['', [Validators.required]],
        emailUser: ['', [Validators.required, Validators.email]],  // Champ nommé email, requis, contrainte EMAIL
        passwordUser: ['', [Validators.required, Validators.minLength(4)]], // Champ nommé email, requis, contrainte longueur
        passwordUserValidation: ['', [Validators.required]],
        termsOfUse: ['', [Validators.required]]
      }, {
        validators: this.passwordMatchValidator
      }
    );
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('passwordUser')?.value;
    const confirmPassword = form.get('passwordUserValidation')?.value;
    if (password !== confirmPassword) {
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.subscirbeForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("subscirbeForm.valid ",this.subscirbeForm.valid);
    console.log("Toutes les valeurs des control du groupe -> subscirbeForm.value ",this.subscirbeForm.value);
    console.log("Recuperer un seul control avec subscirbeForm.get('email')",this.subscirbeForm.get("passwordUser"));
    console.log("Recuperer la validité d'un control avec subscirbeForm.get('email').valid",this.subscirbeForm.get("emailUser")?.valid);
    console.log("Recuperer les erreurs d'un control avec subscirbeForm.get('motDePasse').errors",this.subscirbeForm.get("passwordUser")?.errors);
    console.log("Recuperer un seul control avec subscirbeForm.get('motDePasse')",this.subscirbeForm.get("passwordUser"));


    if (this.subscirbeForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const loginData = { ...this.subscirbeForm.value };
      delete loginData.utilisateurMotDePasseValidation;
      delete loginData.conditionsUtilisation;

      console.log('Données de connexion:', loginData);

      // 5. Simuler un appel API avec setTimeout
      // (Dans un vrai projet, ça serait un appel HTTP)
      setTimeout(() => {

        this.userService.createUser(loginData).subscribe(
          {
            next: (response) => {
              console.log('User login successfully:', response);
              const localArray = [];
              localArray.push(response);
              localStorage.setItem("tokenStorage", JSON.stringify(localArray));
              this.router.navigate(['/connexion']);
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
