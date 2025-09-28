/*
import {Component, ViewChild} from '@angular/core';
import { NgApexchartsModule } from "ng-apexcharts";

import {
  ChartComponent,
  ApexAxisChartSeries,
  ApexChart,
  ApexFill,
  ApexTooltip,
  ApexXAxis,
  ApexLegend,
  ApexDataLabels,
  ApexTitleSubtitle,
  ApexPlotOptions,
  ApexYAxis
} from "ng-apexcharts";

export type ChartOptions = {
  series: ApexAxisChartSeries | any;
  chart: ApexChart | any;
  xaxis: ApexXAxis | any;
  markers: any; //ApexMarkers;
  stroke: any; //ApexStroke;
  yaxis: ApexYAxis | ApexYAxis[] | any;
  plotOptions: ApexPlotOptions | any;
  dataLabels: ApexDataLabels | any;
  colors: string[] | any;
  labels: string[] | number[] | any;
  title: ApexTitleSubtitle | any;
  subtitle: ApexTitleSubtitle | any;
  legend: ApexLegend | any;
  fill: ApexFill | any;
  tooltip: ApexTooltip | any;
};

declare global {
  interface Window {
    Apex: any;
  }
}

const sparkLineData = [
  47,
  45,
  54,
  38,
  56,
  24,
  65,
  31,
  37,
  39,
  62,
  51,
  35,
  41,
  35,
  27,
  93,
  53,
  61,
  27,
  54,
  43,
  19,
  46
];

@Component({
  selector: 'app-card-statistics',
  standalone: true,
  imports: [
    NgApexchartsModule
  ],
  templateUrl: './card-statistics.component.html',
  styleUrl: './card-statistics.component.scss'
})

export class CardStatisticsComponent {
  @ViewChild("chart") chart!: ChartComponent;
  public chartOptions!: Partial<ChartOptions>;
  public chartAreaSparkline1Options: Partial<ChartOptions>;
  public chartAreaSparkline2Options: Partial<ChartOptions>;
  public chartAreaSparkline3Options: Partial<ChartOptions>;
  public commonAreaSparlineOptions: Partial<ChartOptions> = {
    chart: {
      type: "area",
      height: 160,
      sparkline: {
        enabled: true
      }
    },
    stroke: {
      curve: "straight"
    },
    fill: {
      opacity: 0.3
    },
    yaxis: {
      min: 0
    }
  };
  constructor() {
    // setting global apex options which are applied on all charts on the page
    if (typeof window !== 'undefined') {

      window.Apex = {
        stroke: {
          width: 3
        },
        markers: {
          size: 0
        },
        tooltip: {
          fillSeriesColor: true,
          theme: 'dark',
          fixed: {
            enabled: true
          }
        }
      };
    }

    this.chartAreaSparkline1Options = {
      series: [
        {
          name: "Profits",
          data: this.randomizeArray(sparkLineData)
        }
      ],
      colors: ["#233B6E"],
      title: {
        text: "€424,652",
        offsetX: 0,
        style: {
          fontSize: "24px"
        }
      },
      subtitle: {
        text: "Profits",
        offsetX: 0,
        style: {
          fontSize: "14px"
        }
      }
    };

    this.chartAreaSparkline2Options = {
      series: [
        {
          name: "Dépenses",
          data: this.randomizeArray(sparkLineData)
        }
      ],
      colors: ["#D2042D"],
      title: {
        text: "€235,312",
        offsetX: 0,
        style: {
          fontSize: "24px"
        }
      },
      subtitle: {
        text: "Dépenses",
        offsetX: 0,
        style: {
          fontSize: "14px"
        }
      }
    };

    this.chartAreaSparkline3Options = {
      series: [
        {
          name: "Bénéfices",
          data: this.randomizeArray(sparkLineData)
        }
      ],
      colors: ["#008000"],
      title: {
        text: "€135,965",
        offsetX: 0,
        style: {
          fontSize: "24px"
        }
      },
      subtitle: {
        text: "Bénéfices",
        offsetX: 0,
        style: {
          fontSize: "14px"
        }
      }
    };
  }

  public randomizeArray(arg : number[]): number[] {
    var array = arg.slice();
    var currentIndex = array.length,
      temporaryValue,
      randomIndex;

    while (0 !== currentIndex) {
      randomIndex = Math.floor(Math.random() * currentIndex);
      currentIndex -= 1;

      temporaryValue = array[currentIndex];
      array[currentIndex] = array[randomIndex];
      array[randomIndex] = temporaryValue;
    }

    return array;
  }
}
*/
