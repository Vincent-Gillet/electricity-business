import {Component, inject, OnInit} from '@angular/core';
import {LoaderComponent} from "../../../loader/loader.component";
import {MatDialog} from '@angular/material/dialog';
import {Address} from '../../../../../models/address';
import {AddressFormComponent} from '../address-form/address-form.component';
import {AddressComponent} from '../address/address.component';
import {AddressService} from '../../../../../services/address/address.service';

@Component({
  selector: 'app-addresses',
  standalone: true,
  imports: [
    AddressComponent,
    LoaderComponent,
    AddressComponent
  ],
  templateUrl: './addresses.component.html',
  styleUrl: './addresses.component.scss'
})
export class AddressesComponent implements OnInit {
  addresses: Address[] = [];

  addressService: AddressService = inject(AddressService);

  ngOnInit(): void {
    this.addressService.getAddressesByUser().subscribe({
      next: data => {
        this.addresses = data;
        console.log(this.addresses);
      },
      error: err => {
        console.error("Erreur lors de la récupération des adresses :", err);
      },
    })
  }

  private dialog: MatDialog = inject(MatDialog);

  openAddAddress() {
    this.dialog.open(AddressFormComponent)
  }
}
