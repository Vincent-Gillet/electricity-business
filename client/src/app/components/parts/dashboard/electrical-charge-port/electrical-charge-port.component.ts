import {Component, Input} from '@angular/core';
import {Terminal} from '../../../../models/terminal';

@Component({
  selector: 'app-electrical-charge-port',
  standalone: true,
  imports: [],
  templateUrl: './electrical-charge-port.component.html',
  styleUrl: './electrical-charge-port.component.scss'
})
export class ElectricalChargePortComponent {

  @Input() terminal!: Terminal;

  clickDelete() {
    console.log("j'ai cliquer");

    const overlay = document.createElement("div");
    overlay.classList.add("overlay");

    overlay.innerHTML =
      "<div class='delete_box'>" +
        "<p>Êtes-vous sûr de vouloir supprimer votre borne ?</p>" +
        "<div>" +
          "<button id='yes' class='button-style'>Oui</button>" +
          "<button id='no' class='button-style'>Non</button>" +
        "</div>" +
      "</div>";

    document.body.appendChild(overlay);

    const yes = document.querySelector("#yes");
    const no = document.querySelector("#no");

    yes?.addEventListener("click", () => {
      console.log("J'ai appuyer sur oui");
      overlay.remove();
    })

    no?.addEventListener("click", () => {
      console.log("J'ai appuyer sur non");
      overlay.remove();
    })



  }
}
