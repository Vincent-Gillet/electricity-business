import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-global-error',
  standalone: true,
  imports: [],
  templateUrl: './global-error.component.html',
  styleUrl: './global-error.component.scss'
})
export class GlobalErrorComponent implements OnInit {
  @Input() status!: number;
  @Input() message!: string;
  @Input() details?: any;
  @Output() retry = new EventEmitter<void>();

  ngOnInit() {
    console.log('Erreur : ' + this.status + ' - ' + this.details);
  }

  getTitle(): string {
    switch (this.status) {
/*
      case 401: return 'Non autorisé';
*/
      case 404: return 'Page introuvable';
      case 500: return 'Erreur serveur';
      default: return 'Une erreur est survenue';
    }
  }
}
