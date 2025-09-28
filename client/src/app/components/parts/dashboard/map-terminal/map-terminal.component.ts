import {Component, inject, Input, OnInit} from '@angular/core';
import {MapComponent, MarkerComponent, PopupComponent} from '@maplibre/ngx-maplibre-gl';
import {CommonModule, NgStyle} from '@angular/common';
import {NavigationControl} from 'maplibre-gl';
import {GeolocationService} from '@ng-web-apis/geolocation';

@Component({
  selector: 'app-map-terminal',
  standalone: true,
  imports: [
    MapComponent,
    MarkerComponent,
    PopupComponent,
    NgStyle,
    CommonModule
  ],
  templateUrl: './map-terminal.component.html',
  styleUrl: './map-terminal.component.scss'
})
export class MapTerminalComponent implements OnInit{

/*  long: number = 2.287592;*/
/*  lat: number = 48.862725;*/


/*  @Input() long: number = 2.287592;
  @Input() lat: number = 48.862725;*/

  @Input() long: number;
  @Input() lat: number;
  private geolocation: GeolocationService = inject(GeolocationService);

  ngOnInit() {

    this.geolocation.subscribe(data => {
      this.lat = data.coords.latitude;
      this.long = data.coords.longitude;
    })
  }

  // Bornes renvoyer par le composant parent
  @Input() terminals: any[] = [];


  // Propriété pour stocker l'index du popup ouvert
  openedPopupIndex: number | null = null;

  // Map configuration (street map style)
  styleMap: string = 'https://api.maptiler.com/maps/streets-v2/style.json?key=y3jKHE3LdL8zZlzLZ1CY';
/*  // Coordonnées par défaut pour centrer la carte (Paris)
  long = 2.287592;
  lat = 48.862725;*/

  // Fonctionnalité de la carte
  onMapLoad(map: maplibregl.Map): void {
    // Ajout du contrôle de navigation à la carte (zoom et boussole)
    map.addControl(new NavigationControl({ showCompass: true, showZoom: true }), 'top-right');
  }

  // Bouton de réservation
  modalReservation() {}
}
