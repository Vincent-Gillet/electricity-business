/*import { Component } from '@angular/core';

@Component({
  selector: 'app-statistics',
  imports: [],
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.scss'
})
export class StatisticsComponent {


}*/


import { Component, ViewChild } from "@angular/core";
/*
import {CardStatisticsComponent} from '../../card-statistics/card-statistics.component';
*/
/*
import {ColumnChartComponent} from '../../column-chart/column-chart.component';
*/

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  standalone: true,
  imports: [
/*
    CardStatisticsComponent
*/
/*
    ColumnChartComponent
*/
  ],
  styleUrl: './statistics.component.scss'
})
export class StatisticsComponent {

}

