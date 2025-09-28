import {Component, inject, OnInit} from '@angular/core';
import {Car} from '../../../../models/car';
import {CarService} from '../../../../services/car/car.service';
import {LoaderComponent} from '../../loader/loader.component';
import {CarComponent} from '../car/car.component';
import {CarFormComponent} from '../car-form/car-form.component';
import {MatDialog} from '@angular/material/dialog';

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
    this.carService.getCars().subscribe({
      next: data => {
        this.cars = data;
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
