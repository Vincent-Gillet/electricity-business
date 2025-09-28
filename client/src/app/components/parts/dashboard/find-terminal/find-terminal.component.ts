import {Component, inject, OnInit} from '@angular/core';
import {MapTerminalComponent} from '../map-terminal/map-terminal.component';
import {GeolocationService} from '@ng-web-apis/geolocation';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {NominatimService} from '../../../../services/map/nominatim.service';
import {TerminalService} from '../../../../services/terminal/terminal.service';

@Component({
  selector: 'app-find-terminal',
  standalone: true,
  imports: [
    MapTerminalComponent,
    ReactiveFormsModule
  ],
  templateUrl: './find-terminal.component.html',
  styleUrl: './find-terminal.component.scss'
})
export class FindTerminalComponent implements OnInit {


  nominatimService: NominatimService = inject(NominatimService);
  searchTerminalForm: FormGroup;


  // Propriété pour stocker les bornes
  terminals: any[] = [];

  // Injection du service
  private terminalService: TerminalService= inject(TerminalService)
  private geolocation: GeolocationService = inject(GeolocationService);

  long: number = 2.287592;
  lat: number = 48.862725;

  constructor(private fb: FormBuilder, private router: Router) {
    //Création du form
    this.searchTerminalForm = this.fb.group(
      {
        address: [''],
        radius: [''],
        occupied: [null],
        startingDate: [''],
        endingDate: ['']
      }
    );
  }


  ngOnInit() {

    this.geolocation.subscribe(data => {
      this.lat = data.coords.latitude;
      this.long = data.coords.longitude;
    })
    this.chargerTerminals(this.getDefaultParams());
    setTimeout(() => {
      this.chargerTerminals(this.getDefaultParams());
    }, 2000);
  }


  private buildParams(long: number, lat: number): any {
    const formValues = { ...this.searchTerminalForm.value };
    const params = {
      longitude: long,
      latitude: lat,
      ...formValues
    };
    params.radius = (params.radius === '' || params.radius === null || params.radius === undefined) ? 5 : params.radius;
    params.occupied = formValues.occupied === true ? false : undefined;
    delete params.address;
    return params;
  }


  onSubmit() {
    if (this.searchTerminalForm.value.address) {


      console.log("MON FORM EST SOUMIS");
      console.log("loginForm.valid ", this.searchTerminalForm.valid);
      console.log("Toutes les valeurs des control du groupe -> loginForm.value ",this.searchTerminalForm.value);

      console.log("this.searchBorneForm.value : ", this.searchTerminalForm.value);

      const address = this.searchTerminalForm.value.address;


      const encodeAddress = encodeURIComponent(address).replace(/%20/g, '+');
      console.log("encodeAddress : ", encodeAddress);
      this.nominatimService.getGeoLocationWithAdress(encodeAddress).subscribe(
        {
          next: (response) => {
            this.long = response.features[0].geometry.coordinates[0];
            this.lat = response.features[0].geometry.coordinates[1];
            const params = this.buildParams(this.long, this.lat);
            this.chargerTerminals(params);
          },
          error: (error) => {
            console.error('Error login user:', error);
          }
        }
      )
    } else {
      this.geolocation.subscribe(data => {
        this.lat = data.coords.latitude;
        this.long = data.coords.longitude;
        const params = this.buildParams(this.long, this.lat);
        this.chargerTerminals(params);
      })
    }



  };


  getDefaultParams() {
      return {
      longitude : this.long,
      latitude : this.lat,
      radius : !this.searchTerminalForm.get('radius')?.value ? 50 : this.searchTerminalForm.get('radius')?.value,
      occupied : this.searchTerminalForm.get('occupied')?.value === true ? false : undefined,
      startingDate : this.searchTerminalForm.get('startingDate')?.value,
      endingDate : this.searchTerminalForm.get('endingDate')?.value
    };
  }



  // Méthode pour charger les bornes
  chargerTerminals(paramsObj: any) {
    console.log("paramsObj : ", paramsObj);
    const params = Object.entries(paramsObj)
      .map(([key, value]) => {
        if (value !== null && value !== undefined && value !== '') {
          return `${key}=${value}`;
        }
        return undefined;
      })
      .filter(Boolean)
      .join('&');
    console.log("params : ", params);

    // Appel du service pour récupérer les bornes
    this.terminalService.getTerminalsNearby(params).subscribe(data => {
      //Affectation des datas
      this.terminals = data;
    });
  }

  onReset() {
    this.searchTerminalForm.reset();
  }
}
