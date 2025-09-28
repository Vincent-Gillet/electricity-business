import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-car',
  standalone: true,
  imports: [],
  templateUrl: './car.component.html',
  styleUrl: './car.component.scss'
})
export class CarComponent {
  @Input() car: any;

  clickSeeMore(){

  }

  clickUpdate(){

  }

  overlay = document.createElement("div");

  clickDelete() {
    console.log("j'ai cliquer");

    this.overlay.classList.add("overlay");

    this.overlay.innerHTML =
      "<div class='delete_box'>" +
      "<p>Êtes-vous sûr de vouloir supprimer votre borne ?</p>" +
      "<div>" +
      "<button (click)='confirmRemove()' id='yes' class='button-style'>Oui</button>" +
      "<button (click)='cancelRemove()' id='no' class='button-style'>Non</button>" +
      "</div>" +
      "</div>";

    document.body.appendChild(this.overlay);

    const yes = document.querySelector("#yes");
    const no = document.querySelector("#no");

    yes?.addEventListener("click", () => {
      console.log("J'ai appuyer sur oui");
      this.overlay.remove();
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
