import { Component } from '@angular/core';
import {CommonModule, NgForOf, SlicePipe} from "@angular/common";
import {
  NgbPagination,
  NgbPaginationEllipsis,
  NgbPaginationFirst,
  NgbPaginationLast, NgbPaginationModule,
  NgbPaginationNext, NgbPaginationNumber, NgbPaginationPrevious
} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-booking-requests',
  standalone: true,
    imports: [
      CommonModule,
      NgbPaginationModule
    ],
  templateUrl: './booking-requests.component.html',
  styleUrl: './booking-requests.component.scss'
})
export class BookingRequestsComponent {
  page= 1;
  pageSize = 2;

  informations = [
    {
      id: 1,
      date_debut:'07/04/2024 9h30',
      date_fin:'08/04/2024 10h30',
      nom:'Doe',
      prenom:'John',
      numero_borne: '1',
      adresse:'au Clos Fleuris',
      prix:'30'
    },
    {
      id: 2,
      date_debut:'07/02/2025 9h30',
      date_fin:'08/02/2025 10h30',
      nom:'Anderson',
      prenom:'Mark',
      numero_borne: '3',
      adresse:'8 rue de la paix',
      prix:'25'
    },
    {
      id: 3,
      date_debut:'07/12/2024 9h30',
      date_fin:'08/12/2024 10h30',
      nom:'Petit',
      prenom:'Jeanne',
      numero_borne: '2',
      adresse:'au Clos Fleuris',
      prix:'18'
    },
    {
      id: 4,
      date_debut:'07/02/2025 9h30',
      date_fin:'08/02/2025 10h30',
      nom:'Anderson',
      prenom:'Mark',
      numero_borne: '3',
      adresse:'8 rue de la paix',
      prix:'25'
    },
    {
      id: 5,
      date_debut:'07/02/2025 9h30',
      date_fin:'08/02/2025 10h30',
      nom:'Anderson',
      prenom:'Mark',
      numero_borne: '3',
      adresse:'8 rue de la paix',
      prix:'25'
    },
    {
      id: 6,
      date_debut:'07/02/2025 9h30',
      date_fin:'08/02/2025 10h30',
      nom:'Anderson',
      prenom:'Mark',
      numero_borne: '3',
      adresse:'8 rue de la paix',
      prix:'25'
    },
    {
      id: 7,
      date_debut:'07/02/2025 9h30',
      date_fin:'08/02/2025 10h30',
      nom:'Anderson',
      prenom:'Mark',
      numero_borne: '3',
      adresse:'8 rue de la paix',
      prix:'25'
    },
  ];

  numElement = this.informations.length;

  done() {}
}
