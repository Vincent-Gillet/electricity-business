import {Component, inject, Input} from '@angular/core';
import {CurrencyPipe} from "@angular/common";
import {Terminal} from '../../../../models/terminal';
import {TerminalService} from '../../../../services/terminal/terminal.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-terminal',
  standalone: true,
  imports: [
    CurrencyPipe
  ],
  templateUrl: './terminal.component.html',
  styleUrl: './terminal.component.scss'
})
export class TerminalComponent {

  terminalService: TerminalService = inject(TerminalService);
  router: Router = inject(Router);

  @Input() terminal: Terminal;

  private overlay = document.createElement("div");

  clickDelete() {
    console.log("j'ai cliquer");

    this.overlay.classList.add("overlay");

    this.overlay.innerHTML =
      "<div class='delete_box'>" +
      "<p>Êtes-vous sûr de vouloir supprimer votre borne ?</p>" +
      "<div>" +
      "<button id='yes' class='btn button-style'>Oui</button>" +
      "<button id='no' class='btn button-style'>Non</button>" +
      "</div>" +
      "</div>";

    document.body.appendChild(this.overlay);

    const yes = document.querySelector("#yes");
    const no = document.querySelector("#no");

    yes?.addEventListener("click", () => {
      console.log("J'ai appuyer sur oui");
      this.terminalService.deleteTerminalByPublicId(this.terminal.publicId).subscribe({
        next: () => {
          this.overlay.remove();
          const currentUrl = this.router.url;
          this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
            this.router.navigateByUrl(currentUrl);
          });
        },
        error: err => {
          console.error('Erreur lors de la suppression de la borne:', err);
        }
      });
    })

    no?.addEventListener("click", () => {
      console.log("J'ai appuyer sur non");
      this.overlay.remove();
    })
  }
}
