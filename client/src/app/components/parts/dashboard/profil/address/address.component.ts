import {Component, inject, Input} from '@angular/core';
import {LoaderComponent} from '../../../loader/loader.component';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {AddressService} from '../../../../../services/address/address.service';
import {AddressFormComponent} from '../address-form/address-form.component';
import {Address} from '../../../../../models/address';

@Component({
  selector: 'app-address',
  standalone: true,
  imports: [
    LoaderComponent
  ],
  templateUrl: './address.component.html',
  styleUrl: './address.component.scss'
})
export class AddressComponent {
  addressService: AddressService = inject(AddressService);
  @Input() address: any;
  private router: Router
  private dialog: MatDialog = inject(MatDialog);

  clickUpdate(address: Address) {
    this.dialog.open(AddressFormComponent, {
      data: address
    })
  }

  overlay = document.createElement("div");

  clickDelete() {
    console.log("j'ai cliquer");

    this.overlay.classList.add("overlay");

    this.overlay.innerHTML =
      "<div class='delete_box'>" +
      `<p>Êtes-vous sûr de vouloir supprimer votre adresse ${this.address.nameAddress} ?</p>` +
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
      console.log(this.address)

      console.log('publicId de la voiture à supprimer :', this.address.publicId);
      this.addressService.deleteAddressPublicId(this.address.publicId).subscribe();
      this.overlay.remove();
      this.router.navigateByUrl('addresses', { skipLocationChange: true }).then(() => {
        this.router.navigate(['./tableau-de-bord/mes-adresses']);
      });
    })

    no?.addEventListener("click", () => {
      console.log("J'ai appuyer sur non");
      this.overlay.remove();
    })
  }
}
