import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {LoaderComponent} from '../../parts/loader/loader.component';
import {OptionComponent} from '../../parts/dashboard/option/option.component';
import {Option} from '../../../models/option';
import {OptionService} from '../../../services/option/option.service';
import {OptionFormComponent} from '../../parts/dashboard/option-form/option-form.component';

@Component({
  selector: 'app-options',
  standalone: true,
  imports: [
    LoaderComponent,
    OptionComponent
  ],
  templateUrl: './options.component.html',
  styleUrl: './options.component.scss'
})
export class OptionsComponent implements OnInit {
  options: Option[] = [];

  optionService: OptionService = inject(OptionService);

  ngOnInit(): void {
    this.optionService.getOptionsByUser().subscribe({
      next: data => {
        this.options = data;
        console.log(this.options);
      },
      error: err => {
        console.error("Erreur lors de la récupération des options :", err);
      },
    })
  }

  private dialog: MatDialog = inject(MatDialog);

  openAddOption() {
    this.dialog.open(OptionFormComponent)
  }
}
