import {Component, inject, OnInit} from '@angular/core';
import {ElectricalChargePortComponent} from '../electrical-charge-port/electrical-charge-port.component';
import {Terminal} from '../../../../models/terminal';
import {TerminalService} from '../../../../services/terminal/terminal.service';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'app-my-recharging-point',
  standalone: true,
  imports: [
    ElectricalChargePortComponent,
    NgForOf
  ],
  templateUrl: './my-recharging-point.component.html',
  styleUrl: './my-recharging-point.component.scss'
})
export class MyRechargingPointComponent implements OnInit {
  public terminals: Terminal[] = [];

  termianlService: TerminalService = inject(TerminalService);

  ngOnInit() {
    this.termianlService.getTerminals().subscribe((data: Terminal[]) => {
      this.terminals = data;
    });
  }
}
