/*
import { Component, ViewChild } from '@angular/core';
import { NgApexchartsModule } from "ng-apexcharts";

import {
  ApexAxisChartSeries,
  ApexChart,
  ChartComponent,
  ApexDataLabels,
  ApexPlotOptions,
  ApexYAxis,
  ApexLegend,
  ApexStroke,
  ApexXAxis,
  ApexFill,
  ApexTooltip
} from "ng-apexcharts";

export type ChartOptions = {
  series: ApexAxisChartSeries | any;
  chart: ApexChart | any;
  dataLabels: ApexDataLabels | any;
  plotOptions: ApexPlotOptions | any;
  yaxis: ApexYAxis | any;
  xaxis: ApexXAxis | any;
  fill: ApexFill | any;
  tooltip: ApexTooltip | any;
  stroke: ApexStroke | any;
  legend: ApexLegend | any;
  colors: string[] | any;
};

@Component({
  selector: 'app-column-chart',
  standalone: true,
  imports: [
    NgApexchartsModule
  ],
  templateUrl: './column-chart.component.html',
  styleUrl: './column-chart.component.scss'
})
export class ColumnChartComponent {
  @ViewChild("chart") chart!: ChartComponent;
  public chartOptions: Partial<ChartOptions>;

  constructor() {
    this.chartOptions = {
      series: [
        {
          name: "Profits",
          data: [44, 55, 57, 56, 61, 58, 63, 60, 66, 56, 14, 30]
        },
        {
          name: "Dépenses",
          data: [76, 85, 101, 98, 87, 105, 91, 114, 94, 157, 82, 76]
        },
        {
          name: "Bénéfices",
          data: [35, 41, 36, 26, 45, 48, 52, 53, 41, 57, 56, 61]
        }
      ],
      chart: {
        type: "bar",
        height: 350
      },
      plotOptions: {
        bar: {
          horizontal: false,
          columnWidth: "55%",
          endingShape: "rounded"
        }
      },
      dataLabels: {
        enabled: false
      },
      stroke: {
        show: true,
        width: 2,
        colors: ["transparent"]
      },
      xaxis: {
        categories: [
          "Jan",
          "Feb",
          "Mar",
          "Apr",
          "Mai",
          "Juin",
          "Juil",
          "Aoû",
          "Sep",
          "Oct",
          "Nov",
          "Dec"
        ]
      },
      yaxis: {
        title: {
          text: "€"
        }
      },
      fill: {
        opacity: 1,
        colors: ["#233B6E", "#D2042D", "#008000"]
      },
      tooltip: {
        y: {
          formatter: function(val : string) {
            return "€ " + val + "";
          }
        }
      }
    };
  }
}
*/
