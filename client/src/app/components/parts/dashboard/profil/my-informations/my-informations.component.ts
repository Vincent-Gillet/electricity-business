import {Component, inject, OnInit} from '@angular/core';
import {CarComponent} from '../../car/car.component';
import {LoaderComponent} from '../../../loader/loader.component';
import {User} from '../../../../../models/user';
import {AuthService} from '../../../../../services/auth/auth.service';
import {DatePipe} from '@angular/common';
import {CarFormComponent} from '../../car-form/car-form.component';
import {MatDialog} from '@angular/material/dialog';
import {MyInformationsFormComponent} from '../my-informations-form/my-informations-form.component';
import {
  MyInformationsPasswordFormComponent
} from '../my-informations-password-form/my-informations-password-form.component';

@Component({
  selector: 'app-my-informations',
  standalone: true,
  imports: [
    CarComponent,
    LoaderComponent,
    DatePipe
  ],
  templateUrl: './my-informations.component.html',
  styleUrl: './my-informations.component.scss'
})
export class MyInformationsComponent implements OnInit {
  private authService : AuthService = inject(AuthService);
  user: User = null;

  ngOnInit() {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    this.authService.getUserWithToken(accessToken).subscribe({
      next: data => {
        this.user = data;
        console.log(this.user);
      },
      error: err => {
        console.error("Erreur lors de la récupération des informations utilisateur :", err);
      },
    });
  }

  private dialog: MatDialog = inject(MatDialog);

  openUpdateUser(user: User) {
    this.dialog.open(MyInformationsFormComponent,  {
      data: user
    })
  }

  openUpdateUserPassword() {
    this.dialog.open(MyInformationsPasswordFormComponent)
  }
}
