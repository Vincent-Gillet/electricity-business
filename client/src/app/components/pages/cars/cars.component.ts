import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {LoaderComponent} from '../../parts/loader/loader.component';
import {CarComponent} from '../../parts/dashboard/car/car.component';
import {Car} from '../../../models/car';
import {CarService} from '../../../services/car/car.service';
import {CarFormComponent} from '../../parts/dashboard/car-form/car-form.component';

@Component({
  selector: 'app-cars',
  standalone: true,
  imports: [
    LoaderComponent,
    CarComponent
  ],
  templateUrl: './cars.component.html',
  styleUrl: './cars.component.scss'
})
export class CarsComponent implements OnInit{
  cars: Car[] = [];

  carService: CarService = inject(CarService);

  ngOnInit(): void {
    this.carService.getCarsByUser().subscribe({
      next: data => {
        this.cars = data;
        console.log(this.cars);
      },
      error: err => {
        console.error("Erreur lors de la récupération des voitures :", err);
      },
    })
  }


/*  formAdd = false;

  toggleAddForm() {
    this.formAdd = !this.formAdd;
  }*/


  private dialog: MatDialog = inject(MatDialog);

  openAddCar() {
    this.dialog.open(CarFormComponent)
  }

}
