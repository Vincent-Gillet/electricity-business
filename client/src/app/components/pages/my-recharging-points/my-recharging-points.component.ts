import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {LoaderComponent} from '../../parts/loader/loader.component';
import {MyRechargingPointComponent} from '../../parts/dashboard/my-recharging-point/my-recharging-point.component';
import {Place} from '../../../models/place';
import {PlaceService} from '../../../services/place/place.service';
import {MyRechargingPointFormComponent} from '../../parts/dashboard/my-recharging-point-form/my-recharging-point-form.component';

import {
  NgbPagination,
  NgbPaginationEllipsis,
  NgbPaginationFirst,
  NgbPaginationLast,
  NgbPaginationNext, NgbPaginationNumber, NgbPaginationPrevious
} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-my-recharging-points',
  standalone: true,
  imports: [
    MyRechargingPointComponent,
    LoaderComponent,
    NgbPagination,
    NgbPaginationEllipsis,
    NgbPaginationFirst,
    NgbPaginationLast,
    NgbPaginationNext,
    NgbPaginationNumber,
    NgbPaginationPrevious
  ],
  templateUrl: './my-recharging-points.component.html',
  styleUrl: './my-recharging-points.component.scss'
})
export class MyRechargingPointsComponent implements OnInit {
  places: Place[] = [];

  placeService: PlaceService = inject(PlaceService);

  // Pagination
  page= 1;
  pageSize = 2;

  ngOnInit() {
    this.loadPlace();
  }

  private loadPlace(): void {
    this.placeService.getPlacesByUser().subscribe({
      next: data => {
        this.places = data;
        const maxPage = Math.max(1, Math.ceil(this.numElement / this.pageSize));
        if (this.page > maxPage) this.page = maxPage;
      },
      error: err => {
        console.error("Erreur lors de la récupération des lieux :", err);
      },
    })
  }

  // Gestion de la pagination

  public get numElement(): number {
    return this.places.length;
  }

  done() {}

  // Ouvrir le formulaire d'ajout d'un point de recharge
  private dialog: MatDialog = inject(MatDialog);

  openAddCar() {
    this.dialog.open(MyRechargingPointFormComponent)
  }
}
