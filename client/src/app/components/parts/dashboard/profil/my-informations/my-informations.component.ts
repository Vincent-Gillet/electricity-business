import {Component, inject, OnInit} from '@angular/core';
import {LoaderComponent} from '../../../loader/loader.component';
import {User} from '../../../../../models/user';
import {AuthService} from '../../../../../services/auth/auth.service';
import {DatePipe} from '@angular/common';
import {MatDialog} from '@angular/material/dialog';
import {MyInformationsFormComponent} from '../my-informations-form/my-informations-form.component';
import {
  MyInformationsPasswordFormComponent
} from '../my-informations-password-form/my-informations-password-form.component';
import {UserService} from '../../../../../services/user/user.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-my-informations',
  standalone: true,
  imports: [
    LoaderComponent,
    DatePipe
  ],
  templateUrl: './my-informations.component.html',
  styleUrl: './my-informations.component.scss'
})
export class MyInformationsComponent implements OnInit {
  private userService : UserService = inject(UserService);
  router: Router = inject(Router);
  user: User = null;

  ngOnInit() {
    const token = sessionStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    this.userService.getUserWithToken(accessToken).subscribe({
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

  private overlay = document.createElement("div");

  clickDelete() {
    console.log("j'ai cliquer");

    this.overlay.classList.add("overlay");

    this.overlay.innerHTML =
      "<div class='delete_box'>" +
      `<p>Êtes-vous sûr de vouloir supprimer votre compte ?</p>` +
      "<div>" +
      "<button (click)='confirmRemove()' id='yes' class='btn button-style'>Oui</button>" +
      "<button (click)='cancelRemove()' id='no' class='btn button-style'>Non</button>" +
      "</div>" +
      "</div>";

    document.body.appendChild(this.overlay);

    const yes = document.querySelector("#yes");
    const no = document.querySelector("#no");

    yes?.addEventListener("click", () => {
      console.log("J'ai appuyer sur oui");
      console.log(this.user)

      console.log('publicId de la voiture à supprimer :', this.user);
      this.userService.deleteAccount().subscribe({
        next: () => {
          this.overlay.remove();
          const currentUrl = this.router.url;
          this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
            this.router.navigateByUrl(currentUrl);
          });
        },
        error: err => {
          console.error('Erreur lors de la suppression de la voiture:', err);
        }
      });
    })

    no?.addEventListener("click", () => {
      console.log("J'ai appuyer sur non");
      this.overlay.remove();
    })
  }
}
