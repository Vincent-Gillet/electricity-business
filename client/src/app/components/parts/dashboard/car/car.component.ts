import {Component, inject, Input} from '@angular/core';
import {CarService} from '../../../../services/car/car.service';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {CarFormComponent} from '../car-form/car-form.component';
import {Car} from '../../../../models/car';

@Component({
  selector: 'app-car',
  standalone: true,
  imports: [],
  templateUrl: './car.component.html',
  styleUrl: './car.component.scss'
})
export class CarComponent {
  carService: CarService = inject(CarService);
  @Input() car: any;
  private router: Router
  private dialog: MatDialog = inject(MatDialog);

  clickSeeMore(){

  }

  clickUpdate(car: Car) {
    this.dialog.open(CarFormComponent, {
      data: car
    })
  }

  overlay = document.createElement("div");

  clickDelete() {
    console.log("j'ai cliquer");

    this.overlay.classList.add("overlay");

    this.overlay.innerHTML =
      "<div class='delete_box'>" +
      `<p>Êtes-vous sûr de vouloir supprimer votre voiture imatriculé ${this.car.licensePlate} ?</p>` +
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
      console.log(this.car)

      console.log('publicId de la voiture à supprimer :', this.car.publicId);
      this.carService.deleteCarPublicId(this.car.publicId).subscribe();
      this.overlay.remove();
      this.router.navigateByUrl('cars', { skipLocationChange: true }).then(() => {
        this.router.navigate(['./tableau-de-bord/mes-voitures']);
      });
    })

    no?.addEventListener("click", () => {
      console.log("J'ai appuyer sur non");
      this.overlay.remove();
    })
  }

  confirmRemove() {

    /*
        this.overlay.pipe(delay(300)).remove();
    */
  }
}
