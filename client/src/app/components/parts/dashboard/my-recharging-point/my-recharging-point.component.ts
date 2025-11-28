import {Component, inject, Input, OnInit} from '@angular/core';
import {ElectricalChargePortComponent} from '../electrical-charge-port/electrical-charge-port.component';
import {Terminal} from '../../../../models/terminal';
import {TerminalService} from '../../../../services/terminal/terminal.service';
import {NgForOf} from '@angular/common';
import {AddressService} from '../../../../services/address/address.service';
import {Address} from '../../../../models/address';
import {Place} from '../../../../models/place';
import {MyRechargingPointFormComponent} from '../my-recharging-point-form/my-recharging-point-form.component';
import {MatDialog} from '@angular/material/dialog';
import {PlaceService} from '../../../../services/place/place.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-my-recharging-point',
  standalone: true,
  imports: [
    ElectricalChargePortComponent,
    NgForOf
  ],
  templateUrl: './my-recharging-point.component.html',
  styleUrl: './my-recharging-point.component.scss'
})
export class MyRechargingPointComponent implements OnInit {
  public terminals: Terminal[] = [];
  public address: Address;
  private router: Router;
  @Input() public place: Place;
  private dialog: MatDialog = inject(MatDialog);

  private termianlService: TerminalService = inject(TerminalService);
  private addressService: AddressService = inject(AddressService);
  private placeService: PlaceService = inject(PlaceService);

  ngOnInit() {
    this.addressService.getAddressByIdPublic(this.place.publicIdAddress).subscribe(
      address => {
        this.address = address;
      }
    );

    this.termianlService.getTerminals().subscribe(
      terminals => {
        this.terminals = terminals;
      }
    );
  }

  clickUpdate(place: Place) {
    this.dialog.open(MyRechargingPointFormComponent, {
      data: place
    })
  }

  private overlay = document.createElement("div");

  clickDelete(){
    console.log("j'ai cliquer");

    this.overlay.classList.add("overlay");

    this.overlay.innerHTML =
      "<div class='delete_box'>" +
      `<p>Êtes-vous sûr de vouloir supprimer votre lieu avec l'adresse ${this.address.nameAddress} ?</p>` +
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
      console.log(this.place)

      console.log('publicId de la voiture à supprimer :', this.place.publicId);
      this.placeService.deletePlaceByPublicId(this.place.publicId).subscribe();
      this.overlay.remove();
      this.router.navigateByUrl('cars', {skipLocationChange: true}).then(() => {
        this.router.navigate(['./tableau-de-bord/mes-voitures']);
      });
    })

    no?.addEventListener("click", () => {
      console.log("J'ai appuyer sur non");
      this.overlay.remove();
    })
  }

  clickAddTerminal() {

  }
}
