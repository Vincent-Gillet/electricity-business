import {Component, inject, OnInit} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {UserService} from '../../../services/user/user.service';
import {ErrorFromComponent} from '../../parts/error-from/error-from.component';

import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {majorityValidator} from '../../../validator/majority.validator';

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
        pseudo: ['', [Validators.required]],
        dateOfBirth: ['', [Validators.required, majorityValidator(18)]],
        phone: ['', [Validators.required]],
        emailUser: ['', [Validators.required, Validators.email]],  // Champ nommé email, requis, contrainte EMAIL
        passwordUser: ['', [Validators.required, Validators.minLength(8), Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$')]], // contrainte MINLENGTH 8 et pattern (au moins une majuscule, une minuscule, un chiffre et un caractère spécial)
        passwordUserValidation: ['', [Validators.required, Validators.minLength(8), Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$')]],
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
      // Ajouter l'erreur au champ "passwordUserValidation"
      form.get('passwordUserValidation')?.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true }; // Erreur au niveau du groupe (optionnel)
    } else {
      // Supprimer l'erreur si les mots de passe correspondent
      form.get('passwordUserValidation')?.setErrors(null);
      return null;
    }
    return null;
  }

  onSubmit():void {
    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("subscirbeForm.valid ",this.subscirbeForm.valid);
    console.log("Toutes les valeurs des control du groupe -> subscirbeForm.value ",this.subscirbeForm.value);
    console.log("Recuperer un seul control avec subscirbeForm.get('email')",this.subscirbeForm.get("passwordUser"));
    console.log("Recuperer la validité d'un control avec subscirbeForm.get('email').valid",this.subscirbeForm.get("emailUser")?.valid);
    console.log("Recuperer les erreurs d'un control avec subscirbeForm.get('motDePasse').errors",this.subscirbeForm.get("passwordUser")?.errors);
    console.log("Recuperer un seul control avec subscirbeForm.get('motDePasse')",this.subscirbeForm.get("passwordUser"));


    if (this.subscirbeForm.valid) {
      // Activer le state de chargement
      this.isLoading = true;

      // Récupérer les données du formulaire
      const loginData = { ...this.subscirbeForm.value };
      delete loginData.passwordUserValidation;
      delete loginData.termsOfUse;

      console.log('Données de connexion:', loginData);


      this.userService.createUser(loginData).subscribe(
        {
          next: (response) => {
            console.log('User login successfully:', response);
            this.router.navigate(['/connexion']);
          },
          error: (error) => {
            console.error('Error login user:', error);
          }
        }
      );


    }

  }
}
