import { Component } from '@angular/core';
import {ElectricalChargePortComponent} from '../electrical-charge-port/electrical-charge-port.component';
import {MyRechargingPointComponent} from '../my-recharging-point/my-recharging-point.component';

@Component({
  selector: 'app-my-recharging-points',
  standalone: true,
  imports: [
    MyRechargingPointComponent
  ],
  templateUrl: './my-recharging-points.component.html',
  styleUrl: './my-recharging-points.component.scss'
})
export class MyRechargingPointsComponent {

}
