import {Component, inject, Input} from '@angular/core';
import {CarService} from '../../../../services/car/car.service';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {Car} from '../../../../models/car';
import {CarFormComponent} from '../car-form/car-form.component';
import {Option} from '../../../../models/option';
import {OptionFormComponent} from '../option-form/option-form.component';
import {OptionService} from '../../../../services/option/option.service';
import {CurrencyPipe} from '@angular/common';

@Component({
  selector: 'app-option',
  standalone: true,
  imports: [
    CurrencyPipe
  ],
  templateUrl: './option.component.html',
  styleUrl: './option.component.scss'
})
export class OptionComponent {
  optionService: OptionService = inject(OptionService);
  @Input() option: any;
  router: Router = inject(Router);
  private dialog: MatDialog = inject(MatDialog);

  clickSeeMore(){

  }

  clickUpdate(option: Option) {
    this.dialog.open(OptionFormComponent, {
      data: option
    })
  }

  private overlay = document.createElement("div");

  clickDelete() {
    console.log("j'ai cliquer");

    this.overlay.classList.add("overlay");

    this.overlay.innerHTML =
      "<div class='delete_box'>" +
      `<p>Êtes-vous sûr de vouloir supprimer votre option ${this.option.nameOption} ?</p>` +
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
      console.log(this.option)

      console.log('publicId de la voiture à supprimer :', this.option.publicId);
      this.optionService.deleteOptionByPublicId(this.option.publicId).subscribe({
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
